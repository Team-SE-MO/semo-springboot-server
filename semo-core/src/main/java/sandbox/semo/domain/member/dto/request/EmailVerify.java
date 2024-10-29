package sandbox.semo.domain.member.dto.request;

import lombok.Data;

@Data
public class EmailVerify {

    public String email;

    private String authCode;

}
