package sandbox.semo.domain.device.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sandbox.semo.domain.device.entity.DatabaseType;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeviceInfo {

    private String deviceAlias;

    private DatabaseType type;

    private String ip;

    private Long port;

    private String sid;

    private Boolean status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
