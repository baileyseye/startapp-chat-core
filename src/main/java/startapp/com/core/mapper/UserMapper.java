package startapp.com.core.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import startapp.com.core.dto.UserDto;
import startapp.com.core.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "age", source = "age")
    UserDto toDto(User user);

    User toEntity(UserDto dto);
}
