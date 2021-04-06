package tak.server.plugins.utilities;

import java.util.List;
import java.util.Random;
import java.time.Instant;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import atakmap.commoncommo.protobuf.v1.MessageOuterClass;
import atakmap.commoncommo.protobuf.v1.MessageOuterClass.Message;
import atakmap.commoncommo.protobuf.v1.Takmessage.TakMessage;
import tak.server.plugins.PluginConfiguration;
import tak.server.plugins.dto.EventDto;
import tak.server.plugins.dto.EntityDto;
import atakmap.commoncommo.protobuf.v1.Cotevent.CotEvent;
import atakmap.commoncommo.protobuf.v1.DetailOuterClass;

import org.json.JSONArray;
import org.json.JSONML;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class McsCoTConverter {
    
    private static final Logger _logger = LoggerFactory.getLogger(McsCoTConverter.class);
    public static final String FROM_MCS = "fromMcs";
    
    public static Message convertToMessage(EventDto event, PluginConfiguration configuration) {
        Message.Builder messageBuilder = MessageOuterClass.Message.newBuilder();
		TakMessage.Builder payloadBuilder = messageBuilder.getPayloadBuilder();
		CotEvent.Builder cotEventBuilder = payloadBuilder.getCotEventBuilder();
		DetailOuterClass.Detail.Builder detailBuilder = cotEventBuilder.getDetailBuilder();
        		
        @SuppressWarnings("unchecked")
		List<String> callsigns = (List<String>) configuration.getProperty("callsigns");

		@SuppressWarnings("unchecked")
		List<String> uids = (List<String>) configuration.getProperty("uids");

        cotEventBuilder.setUid(event.getUid());
		
        //TEMP stuff - Not in MCS Demo Alert schema
        Instant instant = Instant.now();
        Long timeMs = instant.toEpochMilli() ;
        Long staleMs = timeMs + (5 * 60 * 1000); //Five minutes
        
        cotEventBuilder.setType("a-u");
		cotEventBuilder.setHow("m-r");

		cotEventBuilder.setSendTime(timeMs);
		cotEventBuilder.setStartTime(timeMs);
		cotEventBuilder.setStaleTime(staleMs);
        
        //27.6615493 -81.2769707 - Generally around Avon Park
        Random r = new Random(); 
        double tempLat = 27.6615493 + r.nextDouble() * .08;
        double tempLon = -81.2769707 + r.nextDouble() * .08;

		cotEventBuilder.setLat(tempLat);
		cotEventBuilder.setLon(tempLon);
        
		cotEventBuilder.setHae(9999999);
		cotEventBuilder.setCe(9999999);
		cotEventBuilder.setLe(9999999);

        //Hack to add message, type in CoT details 
        JSONObject detailJsonObject = new JSONObject();
        detailJsonObject.put("message", event.getMessage());
        detailJsonObject.put("type", event.getType());
        detailJsonObject.put("remarks", event.getType() + " " + event.getMessage());
        detailJsonObject.put(FROM_MCS, "true");
        String xmlDetailData = XML.toString(detailJsonObject);
        detailBuilder.setXmlDetail(xmlDetailData);

        return messageBuilder.build();
    }

    public static Message convertToMessage(EntityDto entity, PluginConfiguration configuration) {
        Message.Builder messageBuilder = MessageOuterClass.Message.newBuilder();
		TakMessage.Builder payloadBuilder = messageBuilder.getPayloadBuilder();
		CotEvent.Builder cotEventBuilder = payloadBuilder.getCotEventBuilder();
		DetailOuterClass.Detail.Builder detailBuilder = cotEventBuilder.getDetailBuilder();
        		
        @SuppressWarnings("unchecked")
		List<String> callsigns = (List<String>) configuration.getProperty("callsigns");

		@SuppressWarnings("unchecked")
		List<String> uids = (List<String>) configuration.getProperty("uids");
        
		cotEventBuilder.setUid(entity.getUid());
		cotEventBuilder.setType(entity.getType());
		cotEventBuilder.setHow(entity.getHow());
        
		cotEventBuilder.setSendTime(convertTime(entity.getTime()));
		cotEventBuilder.setStartTime(convertTime(entity.getStart()));
		cotEventBuilder.setStaleTime(convertTime(entity.getStale()));
		cotEventBuilder.setLat(entity.getPoint().getLat());
		cotEventBuilder.setLon(entity.getPoint().getLon());
		cotEventBuilder.setHae(entity.getPoint().getHae());
		cotEventBuilder.setCe(entity.getPoint().getCe());
		cotEventBuilder.setLe(entity.getPoint().getLe());
        detailBuilder.setXmlDetail(entity.getDetail());
        
        if (callsigns != null && !callsigns.isEmpty()) {

            messageBuilder.addAllDestCallsigns(callsigns);
        }

        if (uids != null && !uids.isEmpty()) {

            messageBuilder.addAllDestClientUids(uids);
        }

        return messageBuilder.build();
    }

    private static String getFormattedEntityDetail(String entityDetail) {
        String modifiedXml = entityDetail;
		
		try {
            String tagName = "tagName";
			String childNodes = "childNodes";
			
			JSONObject detailJsonObject = new JSONObject(entityDetail);
			String videoValue = detailJsonObject.optString("video");
			String videoXml = "";
			if (videoValue != "") {
				JSONObject videoJsonObject = new JSONObject();
				videoJsonObject.put(tagName, "__video");
				videoJsonObject.put("url", videoValue);
				detailJsonObject.remove("video");
				videoXml = JSONML.toString(videoJsonObject);
			}

            String imageValue = detailJsonObject.optString("image");
            String imageXml = "";
            if (imageValue != "") {
                JSONObject imageJsonObject = new JSONObject();
                imageJsonObject.put(tagName, "image");
                if (imageValue.contains("data:image")){
                    imageJsonObject.put("mime", imageValue.substring(imageValue.indexOf(':') + 1, imageValue.indexOf(';')));
                    imageJsonObject.put(childNodes, new JSONArray().put(imageValue.substring(imageValue.indexOf(',') + 1)));
                }
                else {
                    imageJsonObject.put("url", imageValue);
                }
                detailJsonObject.remove("image");
                imageXml = JSONML.toString(imageJsonObject);
            }
			
			modifiedXml = XML.toString(detailJsonObject) + videoXml + imageXml;
        }
        catch (Exception e) {
        	//DO Nothing
        }

        return modifiedXml;
    }

    public static EventDto convertToEvent(String json, PluginConfiguration configuration) {
        Gson gson = new Gson();
        EventDto event = gson.fromJson(json, EventDto.class);
        
        JsonElement element = JsonParser.parseString(json);
        JsonObject jObject = element.getAsJsonObject();
        
        return event;
    }

    public static EntityDto convertToEntity(String json, PluginConfiguration configuration) {
        Gson gson = new Gson();
        EntityDto event = gson.fromJson(json, EntityDto.class);
        
        JsonElement element = JsonParser.parseString(json);
        JsonObject jObject = element.getAsJsonObject();
        JsonObject detailJObject = jObject.getAsJsonObject("detail");
        if (detailJObject != null) {
            detailJObject.addProperty(FROM_MCS, "true");
            String jsonDetailData = detailJObject.toString();
            String xmlDetailData = getFormattedEntityDetail(jsonDetailData);
	    	event.setDetail(xmlDetailData);
        }
        
        return event;
    }

    private static Long convertTime(String time) {
        // CoT Protobuf uses "timeMs" units, which is number of milliseconds since
        // 1970-01-01 00:00:00 UTC
        Long timeMs = 0L;
        try {
            Instant instant = Instant.parse( time );
            timeMs = instant.toEpochMilli() ;
        } catch (Exception e) {
            _logger.error("Error parsing time " + time, e);
        }
        
        return timeMs;
    }

    public static Boolean messageIsFromPlugin(String message) {
        return message.contains(CoTMcsConverter.FROM_TAK);
    }
}
