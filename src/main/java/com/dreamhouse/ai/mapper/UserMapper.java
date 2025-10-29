package com.dreamhouse.ai.mapper;

import com.dreamhouse.ai.authentication.dto.AddressDTO;
import com.dreamhouse.ai.authentication.dto.UserDTO;
import com.dreamhouse.ai.authentication.model.entity.UserEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class UserMapper implements Function<UserEntity, UserDTO> {
    @Override
    public UserDTO apply(@NotNull UserEntity userEntity) {
        var userDTO = new UserDTO();
        userDTO.setUserID(userEntity.getUserID());
        userDTO.setUsername(userEntity.getUsername());
        userDTO.setName(userEntity.getName());
        userDTO.setLastname(userEntity.getLastname());

        var addressEntity = userEntity.getBillingAddress();
        if (addressEntity != null) {
            var addressDTO = new AddressDTO();
            addressDTO.setAddressID(addressEntity.getAddressID());
            addressDTO.setBillingCity(addressEntity.getCity());
            addressDTO.setBillingStreet(addressEntity.getStreet());
            addressDTO.setBillingState(addressEntity.getState());
            addressDTO.setBillingZip(addressEntity.getZip());

            userDTO.setBillingAddress(addressDTO);
        }
        return userDTO;
    }
}
