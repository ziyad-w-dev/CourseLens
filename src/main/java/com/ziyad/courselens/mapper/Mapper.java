package com.ziyad.courselens.mapper;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Mapper {

    private final ModelMapper modelMapper;

    public <S, T> T toDto(S source, Class<T> targetClass) {
        return modelMapper.map(source, targetClass);
    }

    public <S, T> T toEntity(S source, Class<T> targetClass) {
        return modelMapper.map(source, targetClass);
    }
}
