package com.herewhite.sdk.domain;

public enum ConverterStatus {
    Idle,
    Created,
    CreateFail,
    Checking,
    WaitingForNextCheck,
    Timeout,
    CheckingFail,
    GetDynamicFail,
    Success,
    Fail,
}
