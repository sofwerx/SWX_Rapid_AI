package tak.server.plugins.utilities;

import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import atakmap.commoncommo.protobuf.v1.Cotevent;
import atakmap.commoncommo.protobuf.v1.Cotevent.CotEvent;
import atakmap.commoncommo.protobuf.v1.ContactOuterClass.Contact;
import atakmap.commoncommo.protobuf.v1.DetailOuterClass.Detail;
import atakmap.commoncommo.protobuf.v1.GroupOuterClass.Group;
import atakmap.commoncommo.protobuf.v1.MessageOuterClass.Message;
import atakmap.commoncommo.protobuf.v1.Precisionlocation.PrecisionLocation;
import atakmap.commoncommo.protobuf.v1.StatusOuterClass.Status;
import atakmap.commoncommo.protobuf.v1.Takmessage.TakMessage;
import atakmap.commoncommo.protobuf.v1.TakvOuterClass.Takv;
import atakmap.commoncommo.protobuf.v1.TrackOuterClass.Track;
import tak.server.plugins.dto.EntityDto;
import tak.server.plugins.dto.PointDto;

public class CoTMcsConverter {
    private static final Logger _logger = LoggerFactory.getLogger(CoTMcsConverter.class);
    public static final String FROM_TAK = "fromTAK";

    public static EntityDto convertToEntityDto(Message message) {
        TakMessage takMessage = message.getPayload();
        CotEvent cotEvent = takMessage.getCotEvent();
        EntityDto entityDto = new EntityDto();
        
        entityDto.setUid(cotEvent.getUid());
        entityDto.setType(cotEvent.getType());
        entityDto.setHow(cotEvent.getHow());
        entityDto.setTime(convertTime(cotEvent.getSendTime()));
        entityDto.setStart(convertTime(cotEvent.getStartTime()));
        entityDto.setStale(convertTime(cotEvent.getStaleTime()));
        entityDto.setHow(cotEvent.getHow());

        PointDto pointDto = new PointDto();
        pointDto.setLat(cotEvent.getLat());
        pointDto.setLon(cotEvent.getLon());
        pointDto.setCe(cotEvent.getCe());
        pointDto.setHae(cotEvent.getHae());
        pointDto.setLe(cotEvent.getLe());
        entityDto.setPoint(pointDto);

        _logger.info("setting details");
        //Detail
        Detail cotDetail = cotEvent.getDetail();
        String xmlDetail = cotDetail.getXmlDetail();
        JSONObject strongTypedDetailJsonObject = new JSONObject(); 
        
        //Contact
        Contact contact = cotDetail.getContact();
        if (contact != null) {
            JSONObject contactJsonObject = new JSONObject();
            contactJsonObject.put("endpoint", Optional.ofNullable(contact.getEndpoint()).orElse(""));
            contactJsonObject.put("callsign", Optional.ofNullable(contact.getCallsign()).orElse(""));
            strongTypedDetailJsonObject.put("contact", contactJsonObject);
        }

        //Group
        Group group = cotDetail.getGroup();
        if(group != null) {
            JSONObject groupJsonObject = new JSONObject();
            groupJsonObject.put("name", Optional.ofNullable(group.getName()).orElse(""));
            groupJsonObject.put("role", Optional.ofNullable(group.getRole()).orElse(""));
            strongTypedDetailJsonObject.put("group", groupJsonObject);
        }

        //PrecisionLocation
        PrecisionLocation precisionLocation = cotDetail.getPrecisionLocation();
        if(precisionLocation != null) {
            JSONObject precisionLocationJsonObject = new JSONObject();
            precisionLocationJsonObject.put("geopointsrc", Optional.ofNullable(precisionLocation.getGeopointsrc()).orElse(""));
            precisionLocationJsonObject.put("altsrc", Optional.ofNullable(precisionLocation.getAltsrc()).orElse(""));
            strongTypedDetailJsonObject.put("precisionLocation", precisionLocationJsonObject);
        }

        //Status
        Status status = cotDetail.getStatus();
        if(status != null) {
            JSONObject statusJsonObject = new JSONObject();
            statusJsonObject.put("battery", status.getBattery());
            strongTypedDetailJsonObject.put("status", statusJsonObject);
        }

        //Takv
        Takv takv = cotDetail.getTakv();
        if (takv != null) {
            JSONObject takvJsonObject = new JSONObject();
            takvJsonObject.put("device", Optional.ofNullable(takv.getDevice()).orElse(""));
            takvJsonObject.put("platform", Optional.ofNullable(takv.getPlatform()).orElse(""));
            takvJsonObject.put("os", Optional.ofNullable(takv.getOs()).orElse(""));
            takvJsonObject.put("version", Optional.ofNullable(takv.getVersion()).orElse(""));
            strongTypedDetailJsonObject.put("takv", takvJsonObject);    
        }

        //Track
        Track track = cotDetail.getTrack();
        if (track != null) {
            JSONObject trackJsonObject = new JSONObject();
            trackJsonObject.put("speed", track.getSpeed());
            trackJsonObject.put("battery", track.getCourse());
            strongTypedDetailJsonObject.put("status", trackJsonObject);
        }

        String additionalXml = XML.toString(strongTypedDetailJsonObject);
        
        entityDto.setDetail(xmlDetail + additionalXml);
        
        //TODO - We're missing the CoT Details stuff not in XML
        /*
            detail {
                xmlDetail: "<uid Droid=\"NORWEGIAN\" nett=\"XX\"/>"
                contact {
                    endpoint: "*:-1:stcp"
                    callsign: "NORWEGIAN"
                }
                group {
                    name: "Cyan"
                    role: "Team Member"
                }
                precisionLocation {
                    geopointsrc: "GPS"
                    altsrc: "GPS"
                }
                status {
                    battery: 70
                }
                takv {
                    device: "SAMSUNG SM-T710"
                    platform: "ATAK"
                    os: "24"
                    version: "4.2.0.1 (38c62f04).1606262829-MIL"
                }
                track {
                    course: 133.30836569850413
                }
            }

        */


        return entityDto;
    }

    public static String convertToJson(EntityDto EntityDto) {
        Gson gson = new Gson();
        JsonElement element = JsonParser.parseString(gson.toJson(EntityDto));
        
        //Manipulate Details Object
        JSONObject detailDataJsonObject = XML.toJSONObject(EntityDto.getDetail());
        detailDataJsonObject.put(FROM_TAK, "true");

        //__video -> video
        JSONObject videoJsonObject = detailDataJsonObject.optJSONObject("__video");
        if (videoJsonObject != null) {
            String url = videoJsonObject.optString("url");
            JSONObject objectConnectionEntry = videoJsonObject.optJSONObject("ConnectionEntry");
            if (url != null) {
                detailDataJsonObject.put("video", url);
            }
            else if (objectConnectionEntry != null) {
                    String protocol = objectConnectionEntry.optString("protocol");
                    String address = objectConnectionEntry.optString("address");
                    int port = objectConnectionEntry.optInt("port");
                    if (port == 0) port = 80;
                    String path = objectConnectionEntry.optString("path");
                    detailDataJsonObject.put("video", protocol + ":////" + address + ":" + port + path);  
            }
        }
        
        String detailJson = detailDataJsonObject.toString();
        
        //Back to gson
        JsonElement detailElement = JsonParser.parseString(detailJson);        
        JsonObject jObject = element.getAsJsonObject();
        jObject.add("detail", detailElement);
       
        return jObject.toString();
    }

    private static String convertTime(Long time) {
        // CoT Protobuf uses "timeMs" units, which is number of milliseconds since
        // 1970-01-01 00:00:00 UTC
        String timeIso = "1970-01-01 00:00:00 UTC";
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
            Instant instant = Instant.ofEpochMilli( time );
            timeIso = formatter.format( instant );
        } catch (Exception e) {
            _logger.error("Error parsing time " + time.toString(), e);
        }
        
        return timeIso;
    }

    public static Boolean messageFromSender(Message message) {
        Boolean fromSender = false;
        try {
                TakMessage takMessage = message.getPayload();
                CotEvent cotEvent = takMessage.getCotEvent();
                fromSender = cotEvent.getDetail().getXmlDetail().contains(McsCoTConverter.FROM_MCS);
        } catch (Exception e) {
            //Do Nothing
        }
        return fromSender;
    }

    public static Boolean messageIsPing(Message message) {
        Boolean isPing = false;
        try {
                TakMessage takMessage = message.getPayload();
                CotEvent cotEvent = takMessage.getCotEvent();
                isPing = cotEvent.getUid().contains("ping");
        } catch (Exception e) {
            //Do Nothing
        }
        return isPing;
    }
}
