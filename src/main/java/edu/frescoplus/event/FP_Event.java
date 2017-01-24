package edu.frescoplus.event;

//FRESCO handled events
//currently, we support PACKET (INCOMMING_FLOW), TCP_CONNECTION, TCP_DISCONNECTION events

public enum FP_Event {
    PACKET,
    TCP,
    TCP_CONNECTION_SUCCESS,
    TCP_CONNECTION_FAIL
}