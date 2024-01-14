package com.michaldrabik.ui_base.viewmodel

import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_base.utilities.events.MessageEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow

interface ChannelsDelegate {

  val messageFlow: Flow<MessageEvent>
  val messageChannel: Channel<MessageEvent>

  val eventFlow: Flow<Event<*>>
  val eventChannel: Channel<Event<*>>
}
