package org.adrianvictor.healerbed.core;

    import org.adrianvictor.healerbed.Main;
    import org.objectweb.asm.ClassReader;
    import org.objectweb.asm.ClassVisitor;
    import org.objectweb.asm.ClassWriter;
    import org.objectweb.asm.MethodVisitor;
    import org.objectweb.asm.Opcodes;

    import net.minecraft.launchwrapper.IClassTransformer;
    import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;

    public class HealerBedTransformer implements IClassTransformer {

        @Override
        public byte[] transform(String className, String transformedName, byte[] basicClass) {
            if (!transformedName.equals("net.minecraft.world.WorldServer")) return basicClass;

            final String targetOwner = className.replace('.', '/');

            ClassReader reader = new ClassReader(basicClass);
            ClassWriter writer = new ClassWriter(reader, 0);

            reader.accept(new ClassVisitor(Opcodes.ASM4, writer) {

                @Override
                public MethodVisitor visitMethod(
                        int access,
                        String methodName,
                        String desc,
                        String signature,
                        String[] exceptions) {

                    MethodVisitor mv = super.visitMethod(access, methodName, desc, signature, exceptions);

                    String srgMethodName = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(targetOwner, methodName, desc);

                    if (!srgMethodName.equals("wakeAllPlayers") && !srgMethodName.equals("func_73053_d")) {
                        return mv;
                    }

                    Main.logger.info("Found wakeAllPlayers (" + methodName + " / " + srgMethodName + ").");

                    return new MethodVisitor(Opcodes.ASM4, mv) {
                        @Override
                        public void visitMethodInsn(
                                int opcode,
                                String owner,
                                String callName,
                                String descriptor,
                                boolean isInterface) {

                            super.visitMethodInsn(opcode, owner, callName, descriptor, isInterface);

                            String srgCallName = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(owner, callName, descriptor);

                            if (opcode == Opcodes.INVOKEVIRTUAL
                                    && ("net/minecraft/entity/player/EntityPlayer".equals(owner) || "zs".equals(owner))
                                    && ("wakeUpPlayer".equals(srgCallName) || "func_70999_a".equals(srgCallName))) {

    				Main.logger.info("Injecting after wakeUpPlayer");

    				mv.visitVarInsn(Opcodes.ALOAD, 2); // local 2 = EntityPlayer

    				mv.visitMethodInsn(
                                        Opcodes.INVOKESTATIC,
                                        "org/adrianvictor/healerbed/Main",
                                        "onPlayerWoken",
                                        "(Lnet/minecraft/entity/player/EntityPlayer;)V",
                                        false
    				);
                            }
    			}
                    };
                }
            }, 0);

            return writer.toByteArray();
        }
    }