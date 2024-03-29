package refactor.naver.reserve.reserveweb_refactor.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class DisplayInfoImageDto {
    private String contentType;
    private boolean deleteFlag;
    private int displayInfoId;
    private int displayInfoImageId;
    private int fileId;
    private String fileName;
    private String saveFileName;
    private LocalDateTime createDate;
    private LocalDateTime modifyDate;
}
