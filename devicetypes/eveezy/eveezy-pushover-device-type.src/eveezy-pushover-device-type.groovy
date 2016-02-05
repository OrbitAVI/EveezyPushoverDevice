/**
 *  Copyright 2015 Eveezy
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */

preferences {
  input("apiKey", "text", title: "API Key", description: "Pushover API Key", required: true)
  input("userKey", "text", title: "User Key", description: "Pushover User Key", required: true)
  input("deviceName", "text", title: "Device Name", description: "Pusherover Device Name", required: true)
  input("emgrRetryInt", "number", title: "Retry Interval", descrtiption: "Pushover Retry Interval", required: true, defaultValue: 30)
  input("emgrExpireInt", "number", title: "Retry Expire", descrtiption: "Pushover Retry Expire Interval", required: true, defaultValue: 90)
}

metadata {
  definition (name: "eveezy: Pushover Device type", namespace: "eveezy", author: "eveezy") {
    capability "Speech Synthesis"
    capability "Switch"

    command "sendPushover"
  }
  
// simulator metadata
  simulator {
    // Todo: Add sim
  }

  tiles {
	// Todo: Add tiles for test message or returns?
      standardTile("switchTile", "device.switch", width: 2, height: 2, canChangeIcon: true) {
          state "off", label: '${name}', action: "switch.on",
              icon: "st.switches.switch.off", backgroundColor: "#ffffff"
          state "on", label: '${name}', action: "switch.off",
              icon: "st.switches.switch.on", backgroundColor: "#E60000"
              }
       main "switchTile"
  }

}

// Switch actions
// todo: Add default switch sounds to prefs. 
def on() {
	sendPushover("Door Open", "High", "pushover");
}

def off() {
	sendPushover("Door Closed", "High", "pushover");
}
// Switch actions

// speechSynthesis Actions
def speak(text) {
	log.debug "Speak requested, $text"
    def messageSound = text.split(";")
    log.debug "message: ${messageSound[0]}, sound: ${messageSound[1]}, priority: ${messageSound[2]}"
    sendPushover("${messageSound[0]}", "${messageSound[2]}", "${messageSound[1]}")
}
// End speechSynthesis actions

def sendPushover(message, priority=0, sound) {
  log.debug "Sending Message: ${message} Priority: ${priority}"

  // Define the initial envelopeMessage keys and values for all messages
  def envelopeMessage = [
    token: "$apiKey",
    user: "$userKey",
    message: "${message}",
    priority: 0,
    sound: "${sound}",
    device: "$deviceName"
  ]

  switch ( priority ) {
    case "Low":
      envelopeMessage['priority'] = -1
      break

    case "High":
      envelopeMessage['priority'] = 1
      break

    case "Emergency":
      envelopeMessage['priority'] = 2
      envelopeMessage['retry'] = "$emgrRetryInt"
      envelopeMessage['expire'] = "$emgrExpireInt"
      break
    }
    
  // Set send params
  def params = [
    uri: "https://api.pushover.net/1/messages.json",
    body: envelopeMessage
  ]

  log.debug envelopeMessage

  if ((apiKey =~ /[A-Za-z0-9]{30}/) && (userKey =~ /[A-Za-z0-9]{30}/))
  {
    log.debug "Pushover: API key '${apiKey}' | User key '${userKey}'"
    httpPost(params){
    response ->
      if(response.status != 200)
      {
        log.error "ERROR: ${response.status}."
      }
      else
      {
        log.debug "[$response.status]"
      }
    }
  }
  else {
  	// Add meaningful error
    log.error "Error"
  }
}