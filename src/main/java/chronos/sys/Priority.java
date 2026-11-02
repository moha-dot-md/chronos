package chronos.sys;
import lombok.Getter;

@Getter
public enum Priority {
    LOW (2),
    HIGH(1);


    private final int value;
    Priority(int value) {
        this.value = value;
    }

}
