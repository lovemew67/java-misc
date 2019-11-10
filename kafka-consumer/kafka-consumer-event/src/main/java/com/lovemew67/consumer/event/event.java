package com.lovemew67.consumer.event;

// import static com.google.common.base.MoreObjects.toStringHelper;

public class event {

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

  // public String toString() {
  //   return toStringHelper(this)
  //     .add("requestId", requestId)
  //     .add("responseId", responseId)
  //     .toString();
  // }
  
}
