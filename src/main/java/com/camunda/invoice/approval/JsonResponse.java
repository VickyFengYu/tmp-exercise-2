package com.camunda.invoice.approval;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class JsonResponse {
  @JsonProperty(value = "id")
  private String id;

  @JsonProperty(value = "bpmn20Xml")
  private String bpmn20Xml;
}
