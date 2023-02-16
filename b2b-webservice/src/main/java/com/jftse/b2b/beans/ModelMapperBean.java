package com.jftse.b2b.beans;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class ModelMapperBean {
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
