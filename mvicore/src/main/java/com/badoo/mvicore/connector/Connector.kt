package com.badoo.mvicore.connector

import io.reactivex.Observable

interface Connector<Out, In>: (Out) -> Observable<In>