-keep enum ** { *; }

-keepattributes *Annotation*,EnclosingMethod,Signature,SourceFile,InnerClasses

-dontwarn com.fasterxml.jackson.databind.**

-keep public class io.github.minerofmillions.packupdater.VersionHistory { *; }