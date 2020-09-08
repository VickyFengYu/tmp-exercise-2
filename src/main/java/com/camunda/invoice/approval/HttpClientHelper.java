package com.camunda.invoice.approval;

import org.apache.commons.lang3.ObjectUtils;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

public class HttpClientHelper {

  private static final String FETCH_XML_BPMN_URL =
      "https://n35ro2ic4d.execute-api.eu-central-1.amazonaws.com/prod/engine-rest/process-definition/key/invoice/xml";

  private static final long TIME_OUT_SECONDS = 6;

  public BpmnModelInstance retrieveBpmnModelInstance()
      throws InterruptedException, ExecutionException, TimeoutException,
          UnsupportedEncodingException, XMLStreamException {

    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(FETCH_XML_BPMN_URL)).build();

    CompletableFuture<HttpResponse<Supplier<JsonResponse>>> response =
        client.sendAsync(request, new JsonBodyHandler<>(JsonResponse.class));

    JsonResponse jsonResponse =
        response.thenApply(obj -> obj.body().get()).get(TIME_OUT_SECONDS, TimeUnit.SECONDS);
    // System.out.println(jsonResponse.getBpmn20Xml());

    if (ObjectUtils.isEmpty(jsonResponse.getBpmn20Xml())) {
      System.exit(-1);
    }

    final XMLStreamReader xmlStreamReader =
        XMLInputFactory.newInstance()
            .createXMLStreamReader(new StringReader(jsonResponse.getBpmn20Xml()));

    String encodingFromXMLDeclaration = xmlStreamReader.getCharacterEncodingScheme();
    // System.out.println("encoding => " + encodingFromXMLDeclaration);

    InputStream stream =
        new ByteArrayInputStream(jsonResponse.getBpmn20Xml().getBytes(encodingFromXMLDeclaration));

    return Bpmn.readModelFromStream(stream);
  }
}
