#!/bin/bash

# Test Agora credentials with curl
APP_ID="b1ea0506ab3b48e7b8858573be4e3ae6"
APP_CERTIFICATE="c04a903217234b44a8e31c76cd74d9e9"
CHANNEL_NAME="testchannel"
UID=123

echo "Testing Agora Credentials"
echo "========================"
echo "App ID: $APP_ID"
echo "App Certificate: $APP_CERTIFICATE"
echo "Channel: $CHANNEL_NAME"
echo "UID: $UID"
echo ""

# Test the token generation endpoint
echo "Testing token generation endpoint..."
curl -X POST "https://fahad-jamil-1.paiza-user-free.cloud/Agora/generate_token.php" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "user_id=$UID&channel_name=$CHANNEL_NAME"

echo ""
echo ""
echo "If you see a token in the response above, the credentials are working."
echo "If you see an error, check the App ID and Certificate."


