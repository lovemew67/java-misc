package com.lovemew67;

import java.io.Serializable;

public class customEvent implements Serializable {

  public String getRequestId() {
    return requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  private String requestId;

  public String getResponseId() {
    return responseId;
  }

  public void setResponseId(String responseId) {
    this.responseId = responseId;
  }

  private String responseId;
  
}
