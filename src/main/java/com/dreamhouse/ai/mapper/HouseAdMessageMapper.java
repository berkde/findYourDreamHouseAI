package com.dreamhouse.ai.mapper;

import com.dreamhouse.ai.house.dto.HouseAdMessageDTO;
import com.dreamhouse.ai.house.model.entity.HouseAdMessageEntity;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class HouseAdMessageMapper implements Function<HouseAdMessageEntity, HouseAdMessageDTO> {
    @Override
    public HouseAdMessageDTO apply(HouseAdMessageEntity entity) {
        var houseAdMessageDTO = new HouseAdMessageDTO();
        houseAdMessageDTO.setHouseAdUid(entity.getHouseAd().getHouseAdUid());
        houseAdMessageDTO.setSubject(entity.getSubject());
        houseAdMessageDTO.setMessage(entity.getMessage());
        houseAdMessageDTO.setSenderEmail(entity.getSenderEmail());
        houseAdMessageDTO.setSenderName(entity.getSenderName());
        houseAdMessageDTO.setSenderPhone(entity.getSenderPhone());
        houseAdMessageDTO.setMessageUid(entity.getMessageUid());
        houseAdMessageDTO.setMessageDate(entity.getMessageDate());
        return houseAdMessageDTO;
    }
}
