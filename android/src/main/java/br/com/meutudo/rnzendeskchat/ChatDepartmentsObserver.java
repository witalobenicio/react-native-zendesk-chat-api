package br.com.meutudo.rnzendeskchat;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.zopim.android.sdk.data.observers.DepartmentsObserver;
import com.zopim.android.sdk.model.Department;

import java.util.Map;

import static com.zopim.android.sdk.model.Department.Status.ONLINE;

public class ChatDepartmentsObserver extends DepartmentsObserver {

    private ReactContext context;

    public ChatDepartmentsObserver(ReactContext context) {
        this.context = context;
    }

    @Override
    protected void update(Map<String, Department> departments) {
        WritableMap status = Arguments.createMap();
        WritableArray departmentsArr = Arguments.createArray();
        boolean isOnline = false;
        for (Map.Entry<String, Department> department : departments.entrySet()) {
            Department departmentValue = department.getValue();
            departmentsArr.pushString(departmentValue.getName());
            if (departmentValue.getStatus() == ONLINE && !status.hasKey("status")) {
                status.putString("status", "ONLINE");
                isOnline = true;
            }
        }
        if (!isOnline) {
            status.putString("status", "OFFLINE");
        }
        status.putArray("departments", departmentsArr);
        this.context
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(ZendeskChatModule.onDepartmentsUpdateEmitter, status);
    }
}
