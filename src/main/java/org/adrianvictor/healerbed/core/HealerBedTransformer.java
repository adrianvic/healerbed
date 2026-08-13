package org.adrianvictor.healerbed.core;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import net.minecraft.launchwrapper.IClassTransformer;

public class HealerBedTransformer implements IClassTransformer {
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (!transformedName.equals("net.minecraft.world.WorldServer")) return basicClass;
		
		ClassReader reader = new ClassReader(basicClass);
		ClassWriter writer = new ClassWriter(reader, 0);
		
		reader.accept(new ClassVisitor(Opcodes.ASM4, writer) {
			
			@Override
			public MethodVisitor visitMethod(
					int access,
					String name,
					String desc,
					String signature,
					String[] exceptions) {
				
				MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
				
				if (!name.equals("wakeAllPlayers")) return mv;
				
				return new MethodVisitor(Opcodes.ASM4, mv) {
					@Override
					public void visitMethodInsn(
							int opcode,
							String owner,
							String name,
							String descriptor,
							boolean isInterface
							) {
						
						super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
						
						if (opcode == Opcodes.INVOKEVIRTUAL
								&& "net/minecraft/entity/player/EntityPlayer".equals(owner)
								&& "wakeUpName".equals(name)
								&& "(ZZZ)V".equals(descriptor)) {
							
							System.out.println("[HEALERBED] Injecting after wakeUpPlayer");
							
							super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
							
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
