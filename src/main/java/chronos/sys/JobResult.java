package chronos.sys;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@NoArgsConstructor
@Data
public class JobResult {
    private JobStatus status;
    private String output;
    private String error;
    private LocalDateTime startPeriod;
    private LocalDateTime endPeriod;
	@Override
	public String toString() {
		return "JobResult [status=" + status + ", output=" + output + ", error=" + error + "]";
	}
    
    

    

    
    
}
