package com.example.automatically_receive_orders

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class MyAccessibilityService : AccessibilityService() {
    private val TAG = "MyAccessibilityService"

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        }
    }

        }

            }
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Dịch vụ bị gián đoạn")
    }
}