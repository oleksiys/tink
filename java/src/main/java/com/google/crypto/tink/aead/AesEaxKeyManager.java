// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////

package com.google.crypto.tink.aead;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.KeyTypeManager;
import com.google.crypto.tink.proto.AesEaxKey;
import com.google.crypto.tink.proto.AesEaxKeyFormat;
import com.google.crypto.tink.proto.KeyData.KeyMaterialType;
import com.google.crypto.tink.subtle.AesEaxJce;
import com.google.crypto.tink.subtle.Random;
import com.google.crypto.tink.subtle.Validators;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.security.GeneralSecurityException;

/**
 * This key manager generates new {@code AesEaxKey} keys and produces new instances of {@code
 * AesEaxJce}.
 */
class AesEaxKeyManager extends KeyTypeManager<AesEaxKey> {
  public AesEaxKeyManager() {
    super(
        AesEaxKey.class,
        new PrimitiveFactory<Aead, AesEaxKey>(Aead.class) {
          @Override
          public Aead getPrimitive(AesEaxKey key) throws GeneralSecurityException {
            return new AesEaxJce(
                key.getKeyValue().toByteArray(), key.getParams().getIvSize());
          }
        });
  }

  @Override
  public String getKeyType() {
    return "type.googleapis.com/google.crypto.tink.AesEaxKey";
  }

  @Override
  public int getVersion() {
    return 0;
  }

  @Override
  public KeyMaterialType keyMaterialType() {
    return KeyMaterialType.SYMMETRIC;
  }

  @Override
  public void validateKey(AesEaxKey key) throws GeneralSecurityException {
    Validators.validateVersion(key.getVersion(), getVersion());
    Validators.validateAesKeySize(key.getKeyValue().size());
    if (key.getParams().getIvSize() != 12 && key.getParams().getIvSize() != 16) {
      throw new GeneralSecurityException("invalid IV size; acceptable values have 12 or 16 bytes");
    }
  }

  @Override
  public AesEaxKey parseKey(ByteString byteString) throws InvalidProtocolBufferException {
    return AesEaxKey.parseFrom(byteString);
  }

  @Override
  public KeyFactory<AesEaxKeyFormat, AesEaxKey> keyFactory() {
    return new KeyFactory<AesEaxKeyFormat, AesEaxKey>(AesEaxKeyFormat.class) {
      @Override
      public void validateKeyFormat(AesEaxKeyFormat format) throws GeneralSecurityException {
        Validators.validateAesKeySize(format.getKeySize());
        if (format.getParams().getIvSize() != 12 && format.getParams().getIvSize() != 16) {
          throw new GeneralSecurityException(
              "invalid IV size; acceptable values have 12 or 16 bytes");
        }
      }

      @Override
      public AesEaxKeyFormat parseKeyFormat(ByteString byteString)
          throws InvalidProtocolBufferException {
        return AesEaxKeyFormat.parseFrom(byteString);
      }

      @Override
      public AesEaxKey createKey(AesEaxKeyFormat format) throws GeneralSecurityException {
        return AesEaxKey.newBuilder()
            .setKeyValue(ByteString.copyFrom(Random.randBytes(format.getKeySize())))
            .setParams(format.getParams())
            .setVersion(getVersion())
            .build();
      }
    };
  }
}
