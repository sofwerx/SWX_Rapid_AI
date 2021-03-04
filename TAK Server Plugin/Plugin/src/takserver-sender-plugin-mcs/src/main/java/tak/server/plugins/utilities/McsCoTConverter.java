package tak.server.plugins.utilities;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import atakmap.commoncommo.protobuf.v1.MessageOuterClass;
import atakmap.commoncommo.protobuf.v1.MessageOuterClass.Message;
import atakmap.commoncommo.protobuf.v1.Takmessage.TakMessage;
import tak.server.plugins.PluginConfiguration;
import tak.server.plugins.dto.EventDto;
import atakmap.commoncommo.protobuf.v1.Cotevent.CotEvent;
import atakmap.commoncommo.protobuf.v1.DetailOuterClass;

import org.json.JSONObject;
import org.json.XML;

public class McsCoTConverter {
    
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
		cotEventBuilder.setType(event.getType());
		cotEventBuilder.setHow(event.getHow());
		cotEventBuilder.setSendTime(event.getTime());
		cotEventBuilder.setStartTime(event.getStart());
		cotEventBuilder.setStaleTime(event.getStale());
		cotEventBuilder.setLat(event.getPoint().getLat());
		cotEventBuilder.setLon(event.getPoint().getLon());
		cotEventBuilder.setHae(event.getPoint().getHae());
		cotEventBuilder.setCe(event.getPoint().getCe());
		cotEventBuilder.setLe(event.getPoint().getLe());
		cotEventBuilder.setHow(event.getHow());
        detailBuilder.setXmlDetail(event.getDetail());

        if (callsigns != null && !callsigns.isEmpty()) {

            messageBuilder.addAllDestCallsigns(callsigns);
        }

        if (uids != null && !uids.isEmpty()) {

            messageBuilder.addAllDestClientUids(uids);
        }

        return messageBuilder.build();
    }

    public static EventDto convertToEvent(String json, PluginConfiguration configuration) {
        Gson gson = new Gson();
        EventDto event = gson.fromJson(json, EventDto.class);
        
        JsonElement element = JsonParser.parseString(json);
        JsonObject jObject = element.getAsJsonObject();
        JsonObject detailJObject = jObject.getAsJsonObject("detail");
        if (detailJObject != null) {
            String jsonDetailData = detailJObject.toString();
            
            //Using org.json here for convenient json->xml serialization
            JSONObject detailJsonObject = new JSONObject(jsonDetailData);
	    	String xmlDetailData = XML.toString(detailJsonObject);
            event.setDetail(xmlDetailData);
        }
        
        return event;
    }
}
