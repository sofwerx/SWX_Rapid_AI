package tak.server.plugins.dto;

import com.google.gson.annotations.SerializedName;

public class EventDto {
    @SerializedName("uid")
	private String _uid = "";
	
	@SerializedName("type")
	private String _type = "";
	
	@SerializedName("time")
	private Long _time = -1L;

	@SerializedName("start")
	private Long _start = -1L;

	@SerializedName("stale")
	private Long _stale = -1L;

	@SerializedName("how")
	private String _how = "";

	@SerializedName("point")
	private PointDto _point = null;

	private String _detail = null;
	
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
    
    public Long getTime() {
        return _time;
    }

    public void setTime(Long time) {
       _time = time;
    }
    
    public Long getStart() {
        return _start;
    }

    public void setStart(Long start) {
       _start = start;
    }
    
    public Long getStale() {
        return _stale;
    }

    public void setStale(Long stale) {
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
