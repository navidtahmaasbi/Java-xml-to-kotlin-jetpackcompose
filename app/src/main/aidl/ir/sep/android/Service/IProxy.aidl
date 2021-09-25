// IProxy.aidl
package ir.sep.android.Service;
// Declare any non-default types here with import statements
interface IProxy {
int VerifyTransaction(in int appId, in String refNum,String resNum);
int ReverseTransaction(in int appId,in String refNum,String resNum);
int PrintByRefNum(String refNum);
int PrintByBitmap(in Bitmap bitmap);
int PrintByString( String string);
}