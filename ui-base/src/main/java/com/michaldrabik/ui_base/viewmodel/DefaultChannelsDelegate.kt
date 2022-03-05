package com.michaldrabik.ui_base.viewmodel

import com.michaldrabik.ui_base.utilities.Event
import com.michaldrabik.ui_base.utilities.MessageEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

@Suppress("PropertyName")
class DefaultChannelsDelegate : ChannelsDelegate {

  override val messageChannel = Channel<MessageEvent>(Channel.BUFFERED)
  override val messageFlow = messageChannel.receiveAsFlow()

  override val eventChannel = Channel<Event<*>>(Channel.BUFFERED)
  override val eventFlow = eventChannel.receiveAsFlow()
}
