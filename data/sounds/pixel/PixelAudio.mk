# Copyright 2018 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

LOCAL_PATH := frameworks/base/data/sounds/pixel

ALARM_FILES := \
         A_real_hoot Cuckoo_clock Full_of_wonder Icicles Loose_change Spokes \
         Bright_morning Early_twilight Gentle_breeze Jump_start Rolling_fog Sunshower

NOTIFICATION_FILES := \
         Beginning Coconuts Duet End_note Gentle_gong Mallet Orders_up Ping Pipes \
         Popcorn Shopkeeper Sticks_and_stones Tuneup Tweeter Twinkle

RINGTONES_FILES := \
          Copycat Crackle Flutterby Hotline Leaps_and_bounds Lollipop Lost_and_found \
          Mash_up Monkey_around Schools_out The_big_adventure Zen_too

UI_FILES := \
          ChargingStarted Dock Effect_Tick InCallNotification KeypressDelete \
          KeypressInvalid KeypressReturn KeypressSpacebar KeypressStandard Lock \
          LowBattery NFCFailure NFCInitiated NFCSuccess NFCTransferComplete NFCTransferInitiated \
          Trusted Undock Unlock VideoRecord VideoStop WirelessChargingStarted audio_end \
          audio_initiate camera_click camera_focus

PRODUCT_COPY_FILES += $(foreach fn,$(ALARM_FILES),\
	$(LOCAL_PATH)/alarms/$(fn).ogg:system/media/audio/alarms/$(fn).ogg)

PRODUCT_COPY_FILES += $(foreach fn,$(NOTIFICATION_FILES),\
	$(LOCAL_PATH)/notifications/$(fn).ogg:system/media/audio/notifications/$(fn).ogg)

PRODUCT_COPY_FILES += $(foreach fn,$(RINGTONE_FILES),\
	$(LOCAL_PATH)/ringtones/$(fn).ogg:system/media/audio/ringtones/$(fn).ogg)

PRODUCT_COPY_FILES += $(foreach fn,$(UI_FILES),\
	$(LOCAL_PATH)/ui/$(fn).ogg:system/media/audio/ui/$(fn).ogg)

