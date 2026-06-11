#!/bin/bash
set -e

SDK=/usr/lib/android-sdk
ANDROID_JAR=$SDK/platforms/android-34/android.jar
BT29=$SDK/build-tools/29.0.3
BT34=$SDK/build-tools/34.0.0
AAPT2=$BT29/aapt2
APKSIGNER=$BT29/apksigner
ZIPALIGN=$BT29/zipalign
ECJ_CP=/usr/share/java/eclipse-jdt-core.jar:/usr/lib/eclipse/plugins/org.eclipse.jdt.core.compiler.batch_3.35.0.jar

PROJECT=/opt/SecretVault
SRC=$PROJECT/app/src/main/java
MANIFEST=$PROJECT/app/src/main/AndroidManifest.xml
BUILD_DIR=$PROJECT/build
GEN_DIR=$BUILD_DIR/gen
OBJ_DIR=$BUILD_DIR/obj
DEX_DIR=$BUILD_DIR/dex
APK_DIR=$BUILD_DIR/apk

rm -rf $BUILD_DIR
mkdir -p $GEN_DIR $OBJ_DIR $DEX_DIR $APK_DIR

echo "=== Compiling Resources ==="
$AAPT2 compile --dir $PROJECT/app/src/main/res -o $BUILD_DIR/resources.zip 2>&1

echo "=== Linking Resources (with R.java) ==="
$AAPT2 link --manifest $MANIFEST -I $ANDROID_JAR \
  -o $BUILD_DIR/raw.apk --auto-add-overlay \
  --java $GEN_DIR $BUILD_DIR/resources.zip 2>&1

echo "=== Extracting raw APK ==="
cd $BUILD_DIR && mkdir -p apk && unzip -q raw.apk -d apk 2>&1 || true
cd $PROJECT

echo "=== Compiling Java ==="
find $SRC -name "*.java" > $BUILD_DIR/sources.txt
find $GEN_DIR -name "*.java" >> $BUILD_DIR/sources.txt
java -cp $ECJ_CP org.eclipse.jdt.internal.compiler.batch.Main \
  -d $OBJ_DIR -classpath $ANDROID_JAR -1.8 \
  @$BUILD_DIR/sources.txt 2>&1

echo "=== Creating JAR ==="
cd $OBJ_DIR && jar cf $BUILD_DIR/classes.jar . && cd $PROJECT

echo "=== Converting to DEX ==="
java -cp /usr/lib/android-sdk/build-tools/34.0.0/lib/d8.jar \
  com.android.tools.r8wrappers.D8Wrapper \
  --lib $ANDROID_JAR --min-api 21 --output $DEX_DIR $BUILD_DIR/classes.jar 2>&1

echo "=== Packaging APK ==="
cp $DEX_DIR/classes.dex $APK_DIR/
cd $APK_DIR && zip -r $BUILD_DIR/unaligned.apk . 2>&1
cd $PROJECT

echo "=== Aligning ==="
$ZIPALIGN -f -p 4 $BUILD_DIR/unaligned.apk $BUILD_DIR/unsigned.apk 2>&1

echo "=== Signing ==="
$APKSIGNER sign --ks /root/.android/debug.keystore --ks-pass pass:android \
  --ks-key-alias androiddebugkey --key-pass pass:android \
  --out $PROJECT/SecretVault.apk $BUILD_DIR/unsigned.apk 2>&1

echo ""
echo "=== APK built! ==="
ls -lh $PROJECT/SecretVault.apk
