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

import tak.server.plugins.dto.EventDto;
import tak.server.plugins.dto.PointDto;

public class CoTMcsConverter {
    private static final Logger _logger = LoggerFactory.getLogger(CoTMcsConverter.class);

    public static EventDto convertToEventDto(Message message) {
        TakMessage takMessage = message.getPayload();
        CotEvent cotEvent = takMessage.getCotEvent();
        EventDto eventDto = new EventDto();
        
        eventDto.setUid(cotEvent.getUid());
        eventDto.setType(cotEvent.getType());
        eventDto.setHow(cotEvent.getHow());
        eventDto.setTime(convertTime(cotEvent.getSendTime()));
        eventDto.setStart(convertTime(cotEvent.getStartTime()));
        eventDto.setStale(convertTime(cotEvent.getStaleTime()));
        eventDto.setHow(cotEvent.getHow());

        PointDto pointDto = new PointDto();
        pointDto.setLat(cotEvent.getLat());
        pointDto.setLon(cotEvent.getLon());
        pointDto.setCe(cotEvent.getCe());
        pointDto.setHae(cotEvent.getHae());
        pointDto.setLe(cotEvent.getLe());
        eventDto.setPoint(pointDto);

        //Detail
        eventDto.setDetail(cotEvent.getDetail().getXmlDetail());
        
        return eventDto;
    }

    public static String convertToJson(EventDto eventDto) {
        Gson gson = new Gson();
        JsonElement element = JsonParser.parseString(gson.toJson(eventDto));
        
        //Using org.json here for convenient xml-> serialization
        JSONObject detailDataJsonObject = XML.toJSONObject(eventDto.getDetail());
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
            _logger.error("Error parsing time", e);
        }
        
        return timeIso;
    }
}
