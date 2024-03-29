package refactor.naver.reserve.reserveweb_refactor.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import refactor.naver.reserve.reserveweb_refactor.dto.CommentDto;
import refactor.naver.reserve.reserveweb_refactor.entity.Comment;
import refactor.naver.reserve.reserveweb_refactor.entity.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {
        CommentImageMapper.class
})
public interface CommentMapper extends EntityMapper<CommentDto, Comment> {

    @Override
    @Mapping(target = "commentId", source = "id")
    @Mapping(target = "commentImages", source = "commentImage")
    @Mapping(target = "reservationDate", source = "reservationInfo.reservationDate")
    @Mapping(target = "reservationEmail", source = "reservationInfo.reservationEmail")
    @Mapping(target = "reservationInfoId", source = "reservationInfo.id")
    @Mapping(target = "reservationName", source = "reservationInfo.reservationName")
    @Mapping(target = "reservationTelephone", source = "reservationInfo.reservationTel")
    @Mapping(target = "createDate", source = "createDate")
    @Mapping(target = "modifyDate", source = "modifyDate")
    CommentDto toDto(Comment comment);

    default List<CommentDto> toDto(List<Comment> comment) {
        return comment.stream().map(this::toDto).collect(Collectors.toList());
    }
}
