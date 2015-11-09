package com.sandinh.couchbase.transcoder

import com.couchbase.client.core.lang.Tuple
import com.couchbase.client.core.message.ResponseStatus
import com.couchbase.client.core.message.kv.MutationToken
import com.couchbase.client.deps.io.netty.buffer.{Unpooled, ByteBuf}
import com.couchbase.client.java.error.TranscodingException
import com.couchbase.client.java.transcoder.AbstractTranscoder
import com.sandinh.couchbase.document.JsDocument
import play.api.libs.json.{Json, JsValue}
import com.couchbase.client.java.transcoder.TranscoderUtils.JSON_COMPAT_FLAGS
import com.couchbase.client.deps.io.netty.util.CharsetUtil.UTF_8
import com.couchbase.client.java.transcoder.TranscoderUtils.hasJsonFlags

/** play-json transcoder */
class JsTranscoder private extends AbstractTranscoder[JsDocument, JsValue] {
  def documentType() = classOf[JsDocument]

  def doEncode(document: JsDocument) = Tuple.create(Unpooled.copiedBuffer(document.content.toString(), UTF_8), JSON_COMPAT_FLAGS)

  def doDecode(id: String, content: ByteBuf, cas: Long, expiry: Int, flags: Int, status: ResponseStatus): JsDocument = {
    if (hasJsonFlags(flags)) {
      val s = content.toString(UTF_8)
      new JsDocument(id, Json.parse(s), expiry, cas)
    } else {
      throw new TranscodingException(s"Flags (0x${Integer.toHexString(flags)}) indicate non-JSON document for id $id, could not decode.")
    }
  }

  def newDocument(id: String, expiry: Int, content: JsValue, cas: Long) = new JsDocument(id, content, expiry, cas)

  override def newDocument(id: String, expiry: Int, content: JsValue, cas: Long, mutationToken: MutationToken) =
    new JsDocument(id, content, expiry, cas, mutationToken)
}

object JsTranscoder extends JsTranscoder
