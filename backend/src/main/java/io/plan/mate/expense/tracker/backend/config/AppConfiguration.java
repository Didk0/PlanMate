package io.plan.mate.expense.tracker.backend.config;

import io.plan.mate.expense.tracker.backend.config.converters.ExpenseParticipantToDtoConverter;
import io.plan.mate.expense.tracker.backend.entities.ExpenseParticipant;
import io.plan.mate.expense.tracker.backend.entities.User;
import io.plan.mate.expense.tracker.backend.payloads.dtos.ExpenseParticipantDto;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfiguration {

  @Bean
  public ModelMapper modelMapper() {

    final ModelMapper modelMapper = new ModelMapper();

    modelMapper
        .typeMap(ExpenseParticipantDto.class, ExpenseParticipant.class)
        .addMappings(
            mapper ->
                mapper
                    .using(
                        context -> {
                          final Long userId = (Long) context.getSource();
                          if (userId == null) {
                            return null;
                          }
                          return User.builder().id(userId).build();
                        })
                    .map(ExpenseParticipantDto::getUserId, ExpenseParticipant::setParticipant));

    modelMapper.addConverter(new ExpenseParticipantToDtoConverter());

    return modelMapper;
  }
}
