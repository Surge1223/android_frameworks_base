package com.google.android.systemui.elmyra;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IElmyraServiceSettingsListener extends IInterface {

    void onGestureDetected() throws RemoteException;

    void onGestureProgress(float f, int i) throws RemoteException;

    public static abstract class Stub extends Binder implements IElmyraServiceSettingsListener {

        public Stub() {
            attachInterface(this, "com.google.android.systemui.elmyra.IElmyraServiceSettingsListener");
        }

        public static IElmyraServiceSettingsListener asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface("com.google.android.systemui.elmyra.IElmyraServiceSettingsListener");
            return (queryLocalInterface == null || !(queryLocalInterface instanceof IElmyraServiceSettingsListener)) ? new Proxy(iBinder) : (IElmyraServiceSettingsListener) queryLocalInterface;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            switch (i) {
                case FLAG_ONEWAY:
                    parcel.enforceInterface("com.google.android.systemui.elmyra.IElmyraServiceSettingsListener");
                    onGestureProgress(parcel.readFloat(), parcel.readInt());
                    return true;
                case 2:
                    parcel.enforceInterface("com.google.android.systemui.elmyra.IElmyraServiceSettingsListener");
                    onGestureDetected();
                    return true;
                case INTERFACE_TRANSACTION:
                    parcel2.writeString("com.google.android.systemui.elmyra.IElmyraServiceSettingsListener");
                    return true;
                default:
                    return super.onTransact(i, parcel, parcel2, i2);
            }
        }

        private static class Proxy implements IElmyraServiceSettingsListener {
            private IBinder mRemote;

            Proxy(IBinder iBinder) {
                mRemote = iBinder;
            }

            public IBinder asBinder() {
                return mRemote;
            }

            public void onGestureDetected() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.systemui.elmyra.IElmyraServiceSettingsListener");
                    mRemote.transact(2, obtain, null, 1);
                } finally {
                    obtain.recycle();
                }
            }

            public void onGestureProgress(float f, int i) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.systemui.elmyra.IElmyraServiceSettingsListener");
                    obtain.writeFloat(f);
                    obtain.writeInt(i);
                    mRemote.transact(1, obtain, null, 1);
                } finally {
                    obtain.recycle();
                }
            }
        }
    }
}

