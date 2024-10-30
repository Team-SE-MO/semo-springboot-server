package sandbox.semo.domain.member.dto.request;

import lombok.Data;

@Data
public class EmailAuthVerify {

    public String email;

    private String authCode;

}
