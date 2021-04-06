package tak.server.plugins.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class EntityDto {
    @SerializedName("uid")
	private String _uid = "";
	
	@SerializedName("type")
	private String _type = "";
	
	@SerializedName("time")
	private String _time = "";

	@SerializedName("start")
	private String _start = "";

	@SerializedName("stale")
	private String _stale = "";

	@SerializedName("how")
	private String _how = "";

	@SerializedName("point")
	private PointDto _point = null;

    private transient String _detail = "";
	
	public String getUid() {
        return _uid;
    }

    public void setUid(String uid) {
       _uid = uid;
    } 
    
    public String getType() {
        return _type;
    }

    public void setType(String type) {
       _type = type;
    }
    
    public String getTime() {
        return _time;
    }

    public void setTime(String time) {
       _time = time;
    }
    
    public String getStart() {
        return _start;
    }

    public void setStart(String start) {
       _start = start;
    }
    
    public String getStale() {
        return _stale;
    }

    public void setStale(String stale) {
       _stale = stale;
    }
    
    public String getHow() {
        return _how;
    }

    public void setHow(String how) {
       _how = how;
    }
    
    public PointDto getPoint() {
        return _point;
    }

    public void setPoint(PointDto point) {
       _point = point;
    }
    
    public String getDetail() {
        return _detail;
    }

    public void setDetail(String detail) {
       _detail = detail;
    }
}
