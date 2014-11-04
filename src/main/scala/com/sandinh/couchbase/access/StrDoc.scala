package com.sandinh.couchbase.access

import com.couchbase.client.java.document.StringDocument

abstract class StrDoc[T] extends DocBase[T, String, StringDocument] {
  protected def createDoc(id: String, expiry: Int, content: String) = StringDocument.create(id, expiry, content)
}

abstract class StrDoc1[T, A] extends StrDoc[T] with Key1[T, A, String, StringDocument]

abstract class StrDoc2[T, A, B] extends StrDoc[T] with Key2[T, A, B, String, StringDocument]
