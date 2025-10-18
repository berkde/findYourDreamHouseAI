package com.dreamhouse.ai.llm.tool;

import com.dreamhouse.ai.house.dto.HouseAdDTO;

import com.dreamhouse.ai.house.repository.HouseAdRepository;
import com.dreamhouse.ai.llm.model.FilterSpec;
import com.dreamhouse.ai.llm.model.HouseAdSpecs;
import dev.langchain4j.agent.tool.Tool;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HouseSearchTool {
    private final HouseAdRepository repository;
    private final ModelMapper mapper;

    public HouseSearchTool(HouseAdRepository repository, ModelMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Tool("Search the database for houses matching the given filters")
    public List<HouseAdDTO> searchHouses(FilterSpec f) {
        var spec = HouseAdSpecs.byFilter(f);
        PageRequest pageRequest = PageRequest.of(0, 1_000, Sort.by("price").ascending());
        return repository.findAll(spec, pageRequest)
                .stream()
                .map(entity -> mapper.map(entity, HouseAdDTO.class) )
                .limit(50)
                .toList();
    }
}
