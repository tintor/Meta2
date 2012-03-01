package tintor.apps.peg;

import java.io.FileOutputStream;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodWriter;
import org.objectweb.asm.Opcodes;

public class Generator implements Opcodes {
	public static void main(final String[] args) throws Exception {

		final ClassWriter cw = new ClassWriter(0);
		cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, "tintor/devel/peg/Dump", null, "java/lang/Object", null);
		{
			final MethodWriter mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main",
					"([Ljava/lang/String;)V", null, null);
			mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
			mv.visitIntInsn(BIPUSH, 120);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V");
			mv.visitInsn(RETURN);
			mv.visitMaxs(2, 1);
		}

		final FileOutputStream out = new FileOutputStream("bin/tintor/devel/peg/dump.class");
		out.write(cw.toByteArray());
		out.close();

		Class.forName("tintor.devel.peg.Dump").getMethod("main", String[].class).invoke(null,
				(Object) new String[0]);
	}
}