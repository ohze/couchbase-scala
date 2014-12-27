package com.sandinh.couchbase.transcoder

import com.couchbase.client.core.lang.{Tuple, Tuple2}
import com.couchbase.client.core.message.ResponseStatus
import com.couchbase.client.deps.io.netty.buffer.{Unpooled, ByteBuf}
import com.couchbase.client.deps.io.netty.util.CharsetUtil.UTF_8
import com.couchbase.client.java.error.TranscodingException
import com.couchbase.client.java.transcoder.{TranscoderUtils, AbstractTranscoder}
import TranscoderUtils.{JSON_COMPAT_FLAGS, JSON_COMMON_FLAGS, hasStringFlags}
import com.sandinh.couchbase.document.CompatStringDocument

/** A transcoder to encode and decode CompatStringDocument */
class CompatStringTranscoder extends AbstractTranscoder[CompatStringDocument, String] {
  def doDecode(id: String, content: ByteBuf, cas: Long, expiry: Int, flags: Int, status: ResponseStatus): CompatStringDocument = {
    lazy val s = content.toString(UTF_8)

    val decoded =
      if (hasStringFlags(flags)) s
      else if ( /* hasCommonFlags(flags) && */ flags == JSON_COMMON_FLAGS) s.substring(1, s.length - 1)
      else if (flags == 0) {
        if (s.startsWith("\"") && s.endsWith("\"")) s.substring(1, s.length - 1)
        else s
      } else throw new TranscodingException(
        s"Flags (0x${Integer.toHexString(flags)}) indicate non-String && non-JsonStringDocument document for id $id, could not decode."
      )

    newDocument(id, expiry, decoded, cas)
  }

  /** encode same as JsonStringTranscoder
    * @see com.couchbase.client.java.transcoder.JsonStringTranscoder#doEncode(com.couchbase.client.java.document.JsonStringDocument) */
  def doEncode(document: CompatStringDocument): Tuple2[ByteBuf, Integer] =
    Tuple.create(Unpooled.copiedBuffer("\"" + document.content + "\"", UTF_8), JSON_COMPAT_FLAGS)

  def newDocument(id: String, expiry: Int, content: String, cas: Long) = new CompatStringDocument(id, content, expiry, cas)

  def documentType() = classOf[CompatStringDocument]
}

object CompatStringTranscoder extends CompatStringTranscoder
