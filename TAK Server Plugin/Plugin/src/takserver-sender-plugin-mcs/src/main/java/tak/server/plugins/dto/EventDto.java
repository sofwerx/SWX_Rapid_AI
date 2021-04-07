package tak.server.plugins.dto;

import com.google.gson.annotations.SerializedName;

public class EventDto {
    @SerializedName("cot_uid")
	private String _uid = "";

    @SerializedName("message")
    private String _message = "";

    @SerializedName("type")
    private String _type = "";

    @SerializedName("point")
	private PointDto _point = new PointDto();

    public String getUid() {
        return _uid;
    }

    public void setUid(String uid) {
       _uid = uid;
    } 

    public String getMessage() {
        return _message;
    }

    public void setMessage(String message) {
        _message = message;
    } 

    public String getType() {
        return _type;
    }

    public void setType(String type) {
        _type = type;
    } 

    public PointDto getPoint() {
        return _point;
    }

    public void setPoint(PointDto point) {
       _point = point;
    }
}
