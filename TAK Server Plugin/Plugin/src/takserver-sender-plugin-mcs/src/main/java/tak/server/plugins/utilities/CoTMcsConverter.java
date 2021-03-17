package tak.server.plugins.utilities;

import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import atakmap.commoncommo.protobuf.v1.Cotevent;
import atakmap.commoncommo.protobuf.v1.Cotevent.CotEvent;
import atakmap.commoncommo.protobuf.v1.MessageOuterClass.Message;
import atakmap.commoncommo.protobuf.v1.Takmessage.TakMessage;

import tak.server.plugins.dto.EntityDto;
import tak.server.plugins.dto.PointDto;

public class CoTMcsConverter {
    private static final Logger _logger = LoggerFactory.getLogger(CoTMcsConverter.class);

    public static EntityDto convertToEntityDto(Message message) {
        TakMessage takMessage = message.getPayload();
        CotEvent cotEvent = takMessage.getCotEvent();
        EntityDto EntityDto = new EntityDto();
        
        EntityDto.setUid(cotEvent.getUid());
        EntityDto.setType(cotEvent.getType());
        EntityDto.setHow(cotEvent.getHow());
        EntityDto.setTime(convertTime(cotEvent.getSendTime()));
        EntityDto.setStart(convertTime(cotEvent.getStartTime()));
        EntityDto.setStale(convertTime(cotEvent.getStaleTime()));
        EntityDto.setHow(cotEvent.getHow());

        PointDto pointDto = new PointDto();
        pointDto.setLat(cotEvent.getLat());
        pointDto.setLon(cotEvent.getLon());
        pointDto.setCe(cotEvent.getCe());
        pointDto.setHae(cotEvent.getHae());
        pointDto.setLe(cotEvent.getLe());
        EntityDto.setPoint(pointDto);

        //Detail
        EntityDto.setDetail(cotEvent.getDetail().getXmlDetail());
        
        return EntityDto;
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

    public static String convertToJson(EntityDto EntityDto) {
        Gson gson = new Gson();
        JsonElement element = JsonParser.parseString(gson.toJson(EntityDto));
        
        //Using org.json here for convenient xml-> serialization
        JSONObject detailDataJsonObject = XML.toJSONObject(EntityDto.getDetail());
        String detailJson = detailDataJsonObject.toString();
        
        //Back to gson
        JsonElement detailJobject = JsonParser.parseString(detailJson);        
        JsonObject jObject = element.getAsJsonObject();
        jObject.add("detail", detailJobject);
        
        return jObject.toString();
    }

    private static String convertTime(Long time) {
        // CoT Protobuf uses "timeMs" units, which is number of milliseconds since
        // 1970-01-01 00:00:00 UTC
        String timeIso = "1970-01-01 00:00:00 UTC";
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.from(ZoneOffset.UTC));
            Instant instant = Instant.ofEpochMilli( time );
            timeIso = formatter.format( instant );
        } catch (Exception e) {
            _logger.error("Error parsing time " + time.toString(), e);
        }
        
        return timeIso;
    }
}
