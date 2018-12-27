
package com.huawei.agentconsole.common.config;

import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;


/**
 * 
 * <p>Title: 重构PKCS5S2ParametersGenerator，支持PBKDF2WithHmacSHA256， 这个才是标准的的PBKDF2+HmacSHA256,之前使用的是PBKDF2+SHA256 </p>
 * <p>Description:  </p>
 * <pre>  </pre>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Huawei Technologies Co.</p>
 * @author j00204006
 * @version V1.0 2017年10月10日
 * @since
 */
public class PKCS5S2ParametersGeneratorEx extends PKCS5S2ParametersGenerator
{
    private Mac hMac = new HMac(new SHA256Digest());

    private void F(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2,
            int paramInt1, byte[] paramArrayOfByte3, byte[] paramArrayOfByte4,
            int paramInt2)
    {
        byte[] arrayOfByte = new byte[this.hMac.getMacSize()];
        KeyParameter localKeyParameter = new KeyParameter(paramArrayOfByte1);
        this.hMac.init(localKeyParameter);
        if (paramArrayOfByte2 != null)
            this.hMac.update(paramArrayOfByte2, 0, paramArrayOfByte2.length);
        this.hMac.update(paramArrayOfByte3, 0, paramArrayOfByte3.length);
        this.hMac.doFinal(arrayOfByte, 0);
        System.arraycopy(arrayOfByte, 0, paramArrayOfByte4, paramInt2,
                arrayOfByte.length);
        if (paramInt1 == 0)
            throw new IllegalArgumentException(
                    "iteration count must be at least 1.");
        for (int i = 1; i < paramInt1; i++)
        {
            this.hMac.init(localKeyParameter);
            this.hMac.update(arrayOfByte, 0, arrayOfByte.length);
            this.hMac.doFinal(arrayOfByte, 0);
            for (int j = 0; j != arrayOfByte.length; j++)
            {
                int tmp172_171 = (paramInt2 + j);
                byte[] tmp172_165 = paramArrayOfByte4;
                tmp172_165[tmp172_171] = (byte) (tmp172_165[tmp172_171] ^ arrayOfByte[j]);
            }
        }
    }

    private void intToOctet(byte[] paramArrayOfByte, int paramInt)
    {
        paramArrayOfByte[0] = (byte) (paramInt >>> 24);
        paramArrayOfByte[1] = (byte) (paramInt >>> 16);
        paramArrayOfByte[2] = (byte) (paramInt >>> 8);
        paramArrayOfByte[3] = (byte) paramInt;
    }

    private byte[] generateDerivedKey(int paramInt)
    {
        int i = this.hMac.getMacSize();
        int j = (paramInt + i - 1) / i;
        byte[] arrayOfByte1 = new byte[4];
        byte[] arrayOfByte2 = new byte[j * i];
        for (int k = 1; k <= j; k++)
        {
            intToOctet(arrayOfByte1, k);
            F(this.password, this.salt, this.iterationCount, arrayOfByte1,
                    arrayOfByte2, (k - 1) * i);
        }
        return arrayOfByte2;
    }

    public CipherParameters generateDerivedParameters(int paramInt)
    {
        paramInt /= 8;
        byte[] arrayOfByte = generateDerivedKey(paramInt);
        return new KeyParameter(arrayOfByte, 0, paramInt);
    }

    public CipherParameters generateDerivedParameters(int paramInt1,
            int paramInt2)
    {
        paramInt1 /= 8;
        paramInt2 /= 8;
        byte[] arrayOfByte = generateDerivedKey(paramInt1 + paramInt2);
        return new ParametersWithIV(
                new KeyParameter(arrayOfByte, 0, paramInt1), arrayOfByte,
                paramInt1, paramInt2);
    }

    public CipherParameters generateDerivedMacParameters(int paramInt)
    {
        return generateDerivedParameters(paramInt);
    }
}

