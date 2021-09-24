package com.sandinh.couchbase.transcoder

import com.couchbase.client.core.lang.{Tuple, Tuple2}
import com.couchbase.client.core.message.ResponseStatus
import com.couchbase.client.core.message.kv.MutationToken
import com.couchbase.client.deps.io.netty.buffer.ByteBuf
import com.couchbase.client.deps.io.netty.util.CharsetUtil.UTF_8
import com.couchbase.client.java.error.TranscodingException
import com.couchbase.client.java.transcoder.{
  TranscoderUtils,
  AbstractTranscoder
}
import TranscoderUtils.{
  STRING_COMMON_FLAGS,
  JSON_COMPAT_FLAGS,
  JSON_COMMON_FLAGS,
  hasStringFlags,
  encodeStringAsUtf8
}
import com.sandinh.couchbase.document.CompatStringDocument

/** A abstract transcoder to decode CompatStringDocument.
  * This class permit decoding a stored document in format of StringDocument OR JsonStringDocument.
  */
abstract class CompatStringTranscoderBase
    extends AbstractTranscoder[CompatStringDocument, String] {
  def doDecode(
    id: String,
    content: ByteBuf,
    cas: Long,
    expiry: Int,
    flags: Int,
    status: ResponseStatus
  ): CompatStringDocument = {
    lazy val s = content.toString(UTF_8)

    val decoded =
      if (hasStringFlags(flags)) s
      else if (/* hasCommonFlags(flags) && */ flags == JSON_COMMON_FLAGS)
        s.substring(1, s.length - 1)
      else if (flags == 0) {
        if (s.startsWith("\"") && s.endsWith("\"")) s.substring(1, s.length - 1)
        else s
      } else
        throw new TranscodingException(
          s"Flags (0x${Integer.toHexString(flags)}) indicate non-String && non-JsonStringDocument document for id $id, could not decode."
        )

    newDocument(id, expiry, decoded, cas)
  }

  def newDocument(id: String, expiry: Int, content: String, cas: Long) =
    new CompatStringDocument(id, content, expiry, cas)

  override def newDocument(
    id: String,
    expiry: Int,
    content: String,
    cas: Long,
    mutationToken: MutationToken
  ) =
    new CompatStringDocument(id, content, expiry, cas, mutationToken)

  def documentType() = classOf[CompatStringDocument]
}

/** A transcoder to encode and decode CompatStringDocument. This class permit:
  * + decoding a stored document in format of StringDocument OR JsonStringDocument.
  * + encoding a String as JsonStringDocument.
  */
class CompatStringTranscoder extends CompatStringTranscoderBase {

  /** encode same as JsonStringTranscoder
    * @see com.couchbase.client.java.transcoder.JsonStringTranscoder#doEncode(com.couchbase.client.java.document.JsonStringDocument)
    */
  def doEncode(document: CompatStringDocument): Tuple2[ByteBuf, Integer] =
    Tuple.create(
      encodeStringAsUtf8("\"" + document.content + "\""),
      JSON_COMPAT_FLAGS
    )
}

object CompatStringTranscoder extends CompatStringTranscoder

/** A transcoder to encode and decode CompatStringDocument. This class permit:
  * + decoding a stored document in format of StringDocument OR JsonStringDocument.
  * + encoding a String as StringDocument.
  */
class CompatStringTranscoderLegacy extends CompatStringTranscoderBase {

  /** encode same as StringTranscoder
    * @see com.couchbase.client.java.transcoder.StringTranscoder#doEncode(com.couchbase.client.java.document.StringDocument)
    */
  def doEncode(document: CompatStringDocument): Tuple2[ByteBuf, Integer] =
    Tuple.create(encodeStringAsUtf8(document.content), STRING_COMMON_FLAGS)
}

object CompatStringTranscoderLegacy extends CompatStringTranscoderLegacy
