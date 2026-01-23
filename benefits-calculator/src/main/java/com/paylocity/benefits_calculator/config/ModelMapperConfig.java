package com.paylocity.benefits_calculator.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for ModelMapper.
 *
 * ModelMapper is used to automatically convert between entities and DTOs,
 * reducing boilerplate code for object mapping.
 *
 * @author Benefits Calculator Team
 * @version 1.0
 */
@Configuration
public class ModelMapperConfig {

    /**
     * Create and configure ModelMapper bean
     *
     * @return configured ModelMapper instance
     */
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        // Configure matching strategy
        // STRICT: Only maps properties with exact matches
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setSkipNullEnabled(true)
                .setAmbiguityIgnored(true);

        return modelMapper;
    }
}