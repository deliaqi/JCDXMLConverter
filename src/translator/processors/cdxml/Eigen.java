// Geometric Tools, Inc.
// http://www.geometrictools.com
// Copyright (c) 1998-2006.  All Rights Reserved
//
// The Wild Magic Library (WM3) source code is supplied under the terms of
// the license agreement
//     http://www.geometrictools.com/License/WildMagic3License.pdf
// and may not be copied or disclosed except in accordance with the terms
// of that agreement.

/*!
 **
 ** Copyright (c) 2007 by John W. Ratcliff mailto:jratcliff@infiniplex.net
 **
 ** Portions of this source has been released with the PhysXViewer application, as well as
 ** Rocket, CreateDynamics, ODF, and as a number of sample code snippets.
 **
 ** If you find this code useful or you are feeling particularily generous I would
 ** ask that you please go to http://www.amillionpixels.us and make a donation
 ** to Troy DeMolay.
 **
 ** DeMolay is a youth group for young men between the ages of 12 and 21.
 ** It teaches strong moral principles, as well as leadership skills and
 ** public speaking.  The donations page uses the 'pay for pixels' paradigm
 ** where, in this case, a pixel is only a single penny.  Donations can be
 ** made for as small as $4 or as high as a $100 block.  Each person who donates
 ** will get a link to their own site as well as acknowledgement on the
 ** donations blog located here http://www.amillionpixels.blogspot.com/
 **
 ** If you wish to contact me you can use the following methods:
 **
 ** Skype Phone: 636-486-4040 (let it ring a long time while it goes through switches)
 ** Skype ID: jratcliff63367
 ** Yahoo: jratcliff63367
 ** AOL: jratcliff1961
 ** email: jratcliff@infiniplex.net
 ** Personal website: http://jratcliffscarab.blogspot.com
 ** Coding Website:   http://codesuppository.blogspot.com
 ** FundRaising Blog: http://amillionpixels.blogspot.com
 ** Fundraising site: http://www.amillionpixels.us
 ** New Temple Site:  http://newtemple.blogspot.com
 **
 **
 ** The MIT license:
 **
 ** Permission is hereby granted, free of charge, to any person obtaining a copy
 ** of this software and associated documentation files (the "Software"), to deal
 ** in the Software without restriction, including without limitation the rights
 ** to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 ** copies of the Software, and to permit persons to whom the Software is furnished
 ** to do so, subject to the following conditions:
 **
 ** The above copyright notice and this permission notice shall be included in all
 ** copies or substantial portions of the Software.
 
 ** THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 ** IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 ** FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 ** AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 ** WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 ** CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 
 */

package translator.processors.cdxml;

public class Eigen {
    
    public double matrix[][] = new double[3][3];
    public double diag[] = new double[3];
    private double m_afSubd[] = new double[3];
    private boolean m_bIsRotation;
    
    public void DecrSortEigenStuff(){
        Tridiagonal(); //diagonalize the matrix.
        QLAlgorithm(); //
        DecreasingSort();
        GuaranteeRotation();
    }
    
    public void Tridiagonal(){
        double fM00 = matrix[0][0];
        double fM01 = matrix[0][1];
        double fM02 = matrix[0][2];
        double fM11 = matrix[1][1];
        double fM12 = matrix[1][2];
        double fM22 = matrix[2][2];
        
        diag[0] = fM00;
        m_afSubd[2] = 0;
        if (fM02 != (double)0.0) {
            double fLength = Math.sqrt(fM01*fM01+fM02*fM02);
            double fInvLength = ((double)1.0)/fLength;
            fM01 *= fInvLength;
            fM02 *= fInvLength;
            double fQ = ((double)2.0)*fM01*fM12+fM02*(fM22-fM11);
            diag[1] = fM11+fM02*fQ;
            diag[2] = fM22-fM02*fQ;
            m_afSubd[0] = fLength;
            m_afSubd[1] = fM12-fM01*fQ;
            matrix[0][0] = (double)1.0;
            matrix[0][1] = (double)0.0;
            matrix[0][2] = (double)0.0;
            matrix[1][0] = (double)0.0;
            matrix[1][1] = fM01;
            matrix[1][2] = fM02;
            matrix[2][0] = (double)0.0;
            matrix[2][1] = fM02;
            matrix[2][2] = -fM01;
            m_bIsRotation = false;
        } else {
            diag[1] = fM11;
            diag[2] = fM22;
            m_afSubd[0] = fM01;
            m_afSubd[1] = fM12;
            matrix[0][0] = (double)1.0;
            matrix[0][1] = (double)0.0;
            matrix[0][2] = (double)0.0;
            matrix[1][0] = (double)0.0;
            matrix[1][1] = (double)1.0;
            matrix[1][2] = (double)0.0;
            matrix[2][0] = (double)0.0;
            matrix[2][1] = (double)0.0;
            matrix[2][2] = (double)1.0;
            m_bIsRotation = true;
        }
    }
    
    boolean QLAlgorithm(){
        int iMaxIter = 32;
        
        for (int i0 = 0; i0 <3; i0++) {
            int i1;
            for (i1 = 0; i1 < iMaxIter; i1++) {
                int i2;
                for (i2 = i0; i2 <= (3-2); i2++) {
                    double fTmp = Math.abs(diag[i2]) + Math.abs(diag[i2+1]);
                    if ( Math.abs(m_afSubd[i2]) + fTmp == fTmp )
                        break;
                }
                if (i2 == i0) {
                    break;
                }
                
                double fG = (diag[i0+1] - diag[i0])/(((double)2.0) * m_afSubd[i0]);
                double fR = Math.sqrt(fG*fG+(double)1.0);
                if (fG < (double)0.0) {
                    fG = diag[i2]-diag[i0]+m_afSubd[i0]/(fG-fR);
                } else {
                    fG = diag[i2]-diag[i0]+m_afSubd[i0]/(fG+fR);
                }
                double fSin = (double)1.0, fCos = (double)1.0, fP = (double)0.0;
                for (int i3 = i2-1; i3 >= i0; i3--) {
                    double fF = fSin*m_afSubd[i3];
                    double fB = fCos*m_afSubd[i3];
                    if (Math.abs(fF) >= Math.abs(fG)) {
                        fCos = fG/fF;
                        fR = Math.sqrt(fCos*fCos+(double)1.0);
                        m_afSubd[i3+1] = fF*fR;
                        fSin = ((double)1.0)/fR;
                        fCos *= fSin;
                    } else {
                        fSin = fF/fG;
                        fR = Math.sqrt(fSin*fSin+(double)1.0);
                        m_afSubd[i3+1] = fG*fR;
                        fCos = ((double)1.0)/fR;
                        fSin *= fCos;
                    }
                    fG = diag[i3+1]-fP;
                    fR = (diag[i3]-fG)*fSin+((double)2.0)*fB*fCos;
                    fP = fSin*fR;
                    diag[i3+1] = fG+fP;
                    fG = fCos*fR-fB;
                    for (int i4 = 0; i4 < 3; i4++) {
                        fF = matrix[i4][i3+1];
                        matrix[i4][i3+1] = fSin*matrix[i4][i3]+fCos*fF;
                        matrix[i4][i3] = fCos*matrix[i4][i3]-fSin*fF;
                    }
                }
                diag[i0] -= fP;
                m_afSubd[i0] = fG;
                m_afSubd[i2] = (double)0.0;
            }
            if (i1 == iMaxIter) {
                return false;
            }
        }
        return true;
    }
    
    public void DecreasingSort(){
        //sort eigenvalues in decreasing order, e[0] >= ... >= e[iSize-1]
        for (int i0 = 0, i1; i0 <= 3-2; i0++) {
            // locate maximum eigenvalue
            i1 = i0;
            double fMax = diag[i1];
            int i2;
            for (i2 = i0+1; i2 < 3; i2++) {
                if (diag[i2] > fMax) {
                    i1 = i2;
                    fMax = diag[i1];
                }
            }
            
            if (i1 != i0) {
                // swap eigenvalues
                diag[i1] = diag[i0];
                diag[i0] = fMax;
                // swap eigenvectors
                for (i2 = 0; i2 < 3; i2++) {
                    double fTmp = matrix[i2][i0];
                    matrix[i2][i0] = matrix[i2][i1];
                    matrix[i2][i1] = fTmp;
                    m_bIsRotation = !m_bIsRotation;
                }
            }
        }
    }
    
    
    public void GuaranteeRotation(){
        if (!m_bIsRotation) {
            // change sign on the first column
            for (int iRow = 0; iRow <3; iRow++) {
                matrix[iRow][0] = -matrix[iRow][0];
            }
        }
    }
    
}
