package capstone.cycle.user.dto;

import capstone.cycle.user.entity.Address;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdDTO {


    @Size(min = 1, max = 10, message = "닉네임은 1자 이상 10자 이하여야 합니다.")
    @Nullable
    private String nickname;

    private Address workAddress;
    private Address homeAddress;
}
