import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;


public class CG_hw5
{
	float x_PRP = 0.0f;
	float y_PRP = 0.0f;
	float z_PRP = 1.0f;
	float x_VRP = 0.0f;
	float y_VRP = 0.0f;
	float z_VRP = 0.0f;
	float x_VPN = 0.0f;
	float y_VPN = 0.0f;
	float z_VPN = -1.0f;
	float x_VUP = 0.0f;
	float y_VUP = 1.0f;
	float z_VUP = 0.0f;
	float umin = -0.7f;
	float vmin = -0.7f;
	float umax = 0.7f;
	float vmax = 0.7f;
	float front_face = 0.6f;
	float back_face = -0.6f;
	float d;
	float z_near;
	float z_far;
	boolean parallel = false;
	float world_x1, world_y1, world_x2, world_y2;
	int view_x1 = 0, view_y1 = 0, view_x2 = 500, view_y2 = 500;
	int width;
	int height;
	String pixels [][];
	float zBuffer[][];
	String input1 = "bound-sprellpsd.smf";
	String input2 = "";
	String input3 = "";
	List<List<Float>> vertices = new ArrayList<List<Float>>();
	List<List<Integer>> faces = new ArrayList<List<Integer>>();
	List<List<List<Float>>> all_polygons = new ArrayList<List<List<Float>>>();
	List<List<List<Float>>> viewport_polygons = new ArrayList<List<List<Float>>>();
	List<List<List<Float>>> intersection_points = new ArrayList<List<List<Float>>>();
	List<List<List<Float>>> edge_list = new ArrayList<List<List<Float>>>();
	List<String> red = new ArrayList<String>();
	List<String> green = new ArrayList<String>();
	List<String> blue = new ArrayList<String>();

	public float findZ(List<List<Float>> polygon, float x_a, float y_a, float x_b, float y_b, float x_p, float y_p)
	{
		float x1 = polygon.get(0).get(0);
		float y1 = polygon.get(0).get(1);
		float z1 = polygon.get(0).get(2);

		float x2 = polygon.get(1).get(0);
		float y2 = polygon.get(1).get(1);
		float z2 = polygon.get(1).get(2);

		float x3 = polygon.get(2).get(0);
		float y3 = polygon.get(2).get(1);
		float z3 = polygon.get(2).get(2);

		float length1_a = length(x_a - x1, y_a - y1, 0);
		float length2_a = length(x2 - x1, y2 - y1, 0);

		float z_a = z1 + (length1_a/length2_a) * (z2 - z1);

		float length1_b = length(x_b - x1, y_b - y1, 0);
		float length2_b = length(x3 - x1, y3 - y1, 0);

		float z_b = z1 + (length1_b/length2_b) * (z3 - z1);

		float length1_p = length(x_p - x_a, y_p - y_a, 0);
		float length2_p = length(x_b - x_a, y_b - y_a, 0);

		float zp = z_a + (length1_p/length2_p) * (z_b - z_a);

		return zp;
	}

	public float getMax(List<List<Float>> list)
	{
		float max = -1;
		for(int i=0; i<list.size(); i++)
		{
			float y = list.get(i).get(1);

			if(Math.round(y) > Math.round(max))
				max = y;
		}
		return max;
	}

	public float getMin(List<List<Float>> list)
	{
		float min = Float.MAX_VALUE;
		for(int i=0; i<list.size(); i++)
		{
			float y = list.get(i).get(1);

			if(Math.round(y) < Math.round(min))
				min = y;
		}
		return min;
	}

	public void scan_fill(List<List<Float>> polygon)
	{
		int color = Math.round(polygon.get(0).get(3));

		for(int i=0; i<intersection_points.size(); i++)
		{
			for(int j=0; j<intersection_points.get(i).size(); j+=2)
			{
				float x1 = intersection_points.get(i).get(j).get(0);
				float y1 = intersection_points.get(i).get(j).get(1);
				float x2 = intersection_points.get(i).get(j+1).get(0);
				float y2 = intersection_points.get(i).get(j+1).get(1);

				float xp = x1;

				while(xp <= x2)
				{

					float pz = findZ(polygon, x1, y1, x2, y2, xp, y1);

					if(xp < 0) xp = 0;
					if(y1 < 0) y1 = 0;
					if(y1 > 500) y1 = 500;
					if(xp > 500) xp = 500;

					if(pz < front_face 
							&& pz > zBuffer[Math.round(y1)][Math.round(xp)])
					{
						zBuffer[Math.round(y1)][Math.round(xp)] = pz;

						int shade = (int)(20 * (pz - z_far)/(z_near - z_far));

						if(shade >= 20) shade = 19;
						if(shade < 0) shade = 0;

						if(color == 0)   // Red
							pixels[Math.round(y1)][Math.round(xp)] = red.get(shade);

						else if(color == 1)  // Green
							pixels[Math.round(y1)][Math.round(xp)] = green.get(shade);

						else                //Blue
							pixels[Math.round(y1)][Math.round(xp)] = blue.get(shade);
					}

					xp++;
				}

			}
		}

	}
	public void sorting()
	{
		//Sorting
		for(int i=0; i<intersection_points.size(); i++)
		{
			Collections.sort(intersection_points.get(i), new Comparator<List<Float>>() 
					{
				public int compare(List<Float> o1, List<Float> o2)
				{
					return o1.get(0).compareTo(o2.get(0));
				}
					});
		}
	}

	public void calculate_intersection(float x1, float y1, float x2, float y2, float y, float ymin)
	{
		List<Float> row = new ArrayList<Float>();

		float dx = x2 - x1;
		float dy = y2 - y1;

		float x = x1 + (dx/dy)*(y - y1);

		row.add(x);
		row.add(y);

		intersection_points.get((int)(y-ymin)).add(row);
	}

	public void polygon_filling()
	{


		//Intializing color lists
		//	red.add("0");
		red.add("1");
		red.add("2");
		red.add("3");
		red.add("4");
		red.add("5");
		red.add("6");
		red.add("7");
		red.add("8");
		red.add("9");
		red.add("a");
		red.add("b");
		red.add("c");
		red.add("d");
		red.add("e");
		red.add("f");
		red.add("g");
		red.add("h");
		red.add("i");
		red.add("j");
		red.add("k");

		//	green.add("0");
		green.add("l");
		green.add("m");
		green.add("n");
		green.add("o");
		green.add("p");
		green.add("q");
		green.add("r");
		green.add("s");
		green.add("t");
		green.add("u");
		green.add("v");
		green.add("w");
		green.add("x");
		green.add("y");
		green.add("z");
		green.add("A");
		green.add("B");
		green.add("C");
		green.add("D");
		green.add("E");
		green.add("F");
		green.add("G");

		//	blue.add("0");
		blue.add("H");
		blue.add("I");
		blue.add("J");
		blue.add("K");
		blue.add("L");
		blue.add("M");
		blue.add("N");
		blue.add("O");
		blue.add("P");
		blue.add("Q");
		blue.add("R");
		blue.add("S");
		blue.add("T");
		blue.add("U");
		blue.add("V");
		blue.add("W");
		blue.add("X");
		blue.add("Y");
		blue.add("Z");
		blue.add("@");

		//Scanline filling
		for(int i=0; i<viewport_polygons.size(); i++)
		{
			float ymax = getMax(viewport_polygons.get(i));
			float ymin = getMin(viewport_polygons.get(i));

			for(int s = (int)(ymin); s<= (int)(ymax); s++)
			{
				edge_list.add(new ArrayList<List<Float>>());
				intersection_points.add(new ArrayList<List<Float>>());
			}

			for(int j=0; j<viewport_polygons.get(i).size() - 1; j++)  
			{

				float ymin_edge = viewport_polygons.get(i).get(j).get(1);
				float ymax_edge = viewport_polygons.get(i).get(j+1).get(1);

				for(int y = (int)(ymin); y<=(int)(ymax); y++)    //For every scanline
				{

					if(((Math.round(y) >= Math.round(ymin_edge) && Math.round(y) < Math.round(ymax_edge)) || 
							(Math.round(y) >= Math.round(ymax_edge) && Math.round(y) < Math.round(ymin_edge)))
							&& Math.round(ymin_edge) != Math.round(ymax_edge))
					{
						List<Float> row = new ArrayList<Float>();
						float x1 = viewport_polygons.get(i).get(j).get(0);
						float x2 = viewport_polygons.get(i).get(j+1).get(0);

						row.add(x1);
						row.add(ymin_edge);
						row.add(x2);
						row.add(ymax_edge);

						edge_list.get((int)(y-ymin)).add(row);
					}
				}  
			}

			for(int y = (int)(ymin); y <= (int)(ymax); y++)
			{
				intersection_points.clear();
				for(int s = (int)(ymin); s<=(int)(ymax); s++)
				{
					intersection_points.add(new ArrayList<List<Float>>());
				}

				for(int j = 0; j<edge_list.get((int)(y-ymin)).size(); j++)
				{
					float x1 = edge_list.get((int)(y-ymin)).get(j).get(0);
					float y1 = edge_list.get((int)(y-ymin)).get(j).get(1);
					float x2 = edge_list.get((int)(y-ymin)).get(j).get(2);
					float y2 = edge_list.get((int)(y-ymin)).get(j).get(3);

					calculate_intersection(x1, y1, x2, y2, y, ymin);
				}

				sorting();
				scan_fill(viewport_polygons.get(i));
			}

			edge_list.clear();
			intersection_points.clear();
		}
	}

	public List<Float> cross_product(float x1, float y1, float z1, float x2, float y2, float z2)
	{
		List<Float> result = new ArrayList<Float>();
		float x = (y1 * z2) - (z1 * y2);
		float y = (z1 * x2) - (x1 * z2);
		float z = (x1 * y2) - (y1 * x2);

		result.add(x);
		result.add(y);
		result.add(z);

		return result;
	}

	public float length(float x, float y, float z)
	{
		return (float) Math.sqrt(x*x + y*y + z*z);
	}

	public float[][] multiply(float[][] a, float[][] b)
	{
		int aRows = a.length;
		int aCols = a[0].length;
		int bCols = b[0].length;

		float [][] result = new float[aRows][bCols];

		for(int i=0; i<aRows; i++)
		{
			for(int j=0; j<bCols; j++)
			{
				for(int k=0; k<aCols; k++)
				{
					result[i][j] += a[i][k] * b[k][j]; 
				}
			}
		}

		return result;
	}

	public void faces_to_polygons()
	{
		for(int i=0; i<faces.size(); i++)
		{
			List<List<Float>> polygon = new ArrayList<List<Float>>();

			int index_1 = faces.get(i).get(0);
			int index_2 = faces.get(i).get(1);
			int index_3 = faces.get(i).get(2);
			int color =   faces.get(i).get(3);

			List<Float> row1 = vertices.get(index_1);
			List<Float> row2 = vertices.get(index_2);
			List<Float> row3 = vertices.get(index_3);
			List<Float> row4 = new ArrayList<Float>();
			row4.add((float) color);

			polygon.add(row1);
			polygon.add(row2);
			polygon.add(row3);
			polygon.add(row1);
			polygon.add(row4);

			all_polygons.add(polygon);
		}
	}

	public void projection()
	{
		if(!parallel)
		{
			for(int i=0; i<vertices.size(); i++)
			{
				float x = vertices.get(i).get(0);
				float y = vertices.get(i).get(1);
				float z = vertices.get(i).get(2);

				float denom = z/d;

				x = x/denom;
				y = y/denom;

				List<Float> row = new ArrayList<Float>();

				row.add(x);
				row.add(y);
				row.add(z);

				vertices.set(i, row);
			}
		}
	}

	public void viewport_transformation()
	{
		if(parallel)
		{
			world_x1 = -1.0f;
			world_y1 = -1.0f;

			world_x2 = 1.0f;
			world_y2 = 1.0f;

			z_near = 0;
			z_far = -1;
		}

		else
		{
			world_x1 = -Math.abs(d);
			world_y1 = -Math.abs(d);

			world_x2 = Math.abs(d);
			world_y2 = Math.abs(d);

			z_near = (z_PRP - front_face)/(back_face - z_PRP);
			z_far = -1;
		}

		//Translation to origin of world window
		List<List<List<Float>>> translated_polygons = new ArrayList<List<List<Float>>>();

		for(int i=0; i<all_polygons.size(); i++)
		{
			List<List<Float>> polygon = new ArrayList<List<Float>>();

			for(int j=0; j<all_polygons.get(i).size(); j++)
			{

				List<Float> row = new ArrayList<Float>();
				float color = all_polygons.get(i).get(4).get(0);

				if(j<4)
				{
					float x = all_polygons.get(i).get(j).get(0);
					float y = all_polygons.get(i).get(j).get(1);
					float z = all_polygons.get(i).get(j).get(2);

					x = x - world_x1;
					y = y - world_y1;


					row.add(x);
					row.add(y);
					row.add(z);
					row.add(color);

					polygon.add(row);
				}
			}

			translated_polygons.add(polygon);
		}

		//Scaling to viewport
		List<List<List<Float>>> scaled_polygons = new ArrayList<List<List<Float>>>();

		for (int i=0; i<translated_polygons.size(); i++)
		{
			List<List<Float>> polygon = new ArrayList<List<Float>>();

			for(int j=0; j<translated_polygons.get(i).size(); j++)
			{
				List<Float> row = new ArrayList<Float>();

				float x = translated_polygons.get(i).get(j).get(0);
				float y = translated_polygons.get(i).get(j).get(1);
				float z = translated_polygons.get(i).get(j).get(2);
				float color = translated_polygons.get(i).get(j).get(3);

				float num_x = view_x2-view_x1;
				float num_y = view_y2-view_y1;
				float den_x = world_x2-world_x1;
				float den_y = world_y2-world_y1;
				x = x * (num_x/den_x);
				y = y * (num_y/den_y);

				row.add(x);
				row.add(y);
				row.add(z);
				row.add(color);

				polygon.add(row);	
			}
			scaled_polygons.add(polygon);
		}

		//Translating to viewport origin
		for(int i=0; i<scaled_polygons.size(); i++)
		{
			List<List<Float>> polygon = new ArrayList<List<Float>>();

			for(int j=0; j<scaled_polygons.get(i).size(); j++)
			{

				List<Float> row = new ArrayList<Float>();

				float x = scaled_polygons.get(i).get(j).get(0);
				float y = scaled_polygons.get(i).get(j).get(1);
				float z = scaled_polygons.get(i).get(j).get(2);
				float color = scaled_polygons.get(i).get(j).get(3);

				x = x + view_x1;
				y = y + view_y1;
				row.add(x);
				row.add(y);
				row.add(z);
				row.add(color);

				polygon.add(row);
			}

			viewport_polygons.add(polygon);
		}
	}

	public void output() throws FileNotFoundException, UnsupportedEncodingException
	{
		System.out.println("/*XPM*/");
		System.out.println("static char *sco100[] = { ");
		System.out.println("/* width height num_colors chars_per_pixel */ ");
		System.out.println("\""+ width + " " + height + " " + "61" + " " + "1" + "\"" + ",");
		System.out.println("/*colors*/");
		//Black
		System.out.println("\""+ "0" + " " + "c" + " " + "#" + "000000" + "\"" + "," );
		//20 shades of Red
		System.out.println("\""+ "1" + " " + "c" + " " + "#" + "0d0000" + "\"" + "," );
		System.out.println("\""+ "2" + " " + "c" + " " + "#" + "1a0000" + "\"" + "," );
		System.out.println("\""+ "3" + " " + "c" + " " + "#" + "270000" + "\"" + "," );
		System.out.println("\""+ "4" + " " + "c" + " " + "#" + "340000" + "\"" + "," );
		System.out.println("\""+ "5" + " " + "c" + " " + "#" + "410000" + "\"" + "," );
		System.out.println("\""+ "6" + " " + "c" + " " + "#" + "4e0000" + "\"" + "," );
		System.out.println("\""+ "7" + " " + "c" + " " + "#" + "5b0000" + "\"" + "," );
		System.out.println("\""+ "8" + " " + "c" + " " + "#" + "680000" + "\"" + "," );
		System.out.println("\""+ "9" + " " + "c" + " " + "#" + "750000" + "\"" + "," );
		System.out.println("\""+ "a" + " " + "c" + " " + "#" + "820000" + "\"" + "," );
		System.out.println("\""+ "b" + " " + "c" + " " + "#" + "8f0000" + "\"" + "," );
		System.out.println("\""+ "c" + " " + "c" + " " + "#" + "9c0000" + "\"" + "," );
		System.out.println("\""+ "d" + " " + "c" + " " + "#" + "a90000" + "\"" + "," );
		System.out.println("\""+ "e" + " " + "c" + " " + "#" + "b60000" + "\"" + "," );
		System.out.println("\""+ "f" + " " + "c" + " " + "#" + "c30000" + "\"" + "," );
		System.out.println("\""+ "g" + " " + "c" + " " + "#" + "d00000" + "\"" + "," );
		System.out.println("\""+ "h" + " " + "c" + " " + "#" + "dd0000" + "\"" + "," );
		System.out.println("\""+ "i" + " " + "c" + " " + "#" + "ea0000" + "\"" + "," );
		System.out.println("\""+ "j" + " " + "c" + " " + "#" + "f70000" + "\"" + "," );
		System.out.println("\""+ "k" + " " + "c" + " " + "#" + "ff0000" + "\"" + "," );
		//20 shades of Green
		System.out.println("\""+ "l" + " " + "c" + " " + "#" + "000d00" + "\"" + "," );
		System.out.println("\""+ "m" + " " + "c" + " " + "#" + "001a00" + "\"" + "," );
		System.out.println("\""+ "n" + " " + "c" + " " + "#" + "002700" + "\"" + "," );
		System.out.println("\""+ "o" + " " + "c" + " " + "#" + "003400" + "\"" + "," );
		System.out.println("\""+ "p" + " " + "c" + " " + "#" + "004100" + "\"" + "," );
		System.out.println("\""+ "q" + " " + "c" + " " + "#" + "004e00" + "\"" + "," );
		System.out.println("\""+ "r" + " " + "c" + " " + "#" + "005b00" + "\"" + "," );
		System.out.println("\""+ "s" + " " + "c" + " " + "#" + "006800" + "\"" + "," );
		System.out.println("\""+ "t" + " " + "c" + " " + "#" + "007500" + "\"" + "," );
		System.out.println("\""+ "u" + " " + "c" + " " + "#" + "008200" + "\"" + "," );
		System.out.println("\""+ "v" + " " + "c" + " " + "#" + "008f00" + "\"" + "," );
		System.out.println("\""+ "y" + " " + "c" + " " + "#" + "009c00" + "\"" + "," );
		System.out.println("\""+ "z" + " " + "c" + " " + "#" + "00a900" + "\"" + "," );
		System.out.println("\""+ "A" + " " + "c" + " " + "#" + "00b600" + "\"" + "," );
		System.out.println("\""+ "B" + " " + "c" + " " + "#" + "00c300" + "\"" + "," );
		System.out.println("\""+ "C" + " " + "c" + " " + "#" + "00d000" + "\"" + "," );
		System.out.println("\""+ "D" + " " + "c" + " " + "#" + "00dd00" + "\"" + "," );
		System.out.println("\""+ "E" + " " + "c" + " " + "#" + "00ea00" + "\"" + "," );
		System.out.println("\""+ "F" + " " + "c" + " " + "#" + "00f700" + "\"" + "," );
		System.out.println("\""+ "G" + " " + "c" + " " + "#" + "00ff00" + "\"" + "," );
		//20 shades of Blue
		System.out.println("\""+ "H" + " " + "c" + " " + "#" + "00000d" + "\"" + "," );
		System.out.println("\""+ "I" + " " + "c" + " " + "#" + "00001a" + "\"" + "," );
		System.out.println("\""+ "J" + " " + "c" + " " + "#" + "000027" + "\"" + "," );
		System.out.println("\""+ "K" + " " + "c" + " " + "#" + "000034" + "\"" + "," );
		System.out.println("\""+ "L" + " " + "c" + " " + "#" + "000041" + "\"" + "," );
		System.out.println("\""+ "M" + " " + "c" + " " + "#" + "00004e" + "\"" + "," );
		System.out.println("\""+ "N" + " " + "c" + " " + "#" + "00005b" + "\"" + "," );
		System.out.println("\""+ "O" + " " + "c" + " " + "#" + "000068" + "\"" + "," );
		System.out.println("\""+ "P" + " " + "c" + " " + "#" + "000075" + "\"" + "," );
		System.out.println("\""+ "Q" + " " + "c" + " " + "#" + "000082" + "\"" + "," );
		System.out.println("\""+ "R" + " " + "c" + " " + "#" + "00008f" + "\"" + "," );
		System.out.println("\""+ "S" + " " + "c" + " " + "#" + "00009c" + "\"" + "," );
		System.out.println("\""+ "T" + " " + "c" + " " + "#" + "0000a9" + "\"" + "," );
		System.out.println("\""+ "U" + " " + "c" + " " + "#" + "0000b6" + "\"" + "," );
		System.out.println("\""+ "V" + " " + "c" + " " + "#" + "0000c3" + "\"" + "," );
		System.out.println("\""+ "W" + " " + "c" + " " + "#" + "0000d0" + "\"" + "," );
		System.out.println("\""+ "X" + " " + "c" + " " + "#" + "0000dd" + "\"" + "," );
		System.out.println("\""+ "Y" + " " + "c" + " " + "#" + "0000ea" + "\"" + "," );
		System.out.println("\""+ "Z" + " " + "c" + " " + "#" + "0000f7" + "\"" + "," );
		System.out.println("\""+ "@" + " " + "c" + " " + "#" + "0000ff" + "\"" + "," );

		System.out.println("/*pixels*/");

		for (int i=0; i<height; i++)
		{
			System.out.print("\"");
			for(int j=0; j<width; j++)
			{
				System.out.print(pixels[height-i-1][j]);
			}
			if(i == height - 1)
				System.out.print("\"");
			else
				System.out.print("\"" + ",");

			System.out.println();
		}

		System.out.println("};");
	}

	public void transformations()
	{
		//Translation to -VRP
		float [][] t_VRP = new float[4][4];

		t_VRP[0][0] = 1;
		t_VRP[0][1] = 0;
		t_VRP[0][2] = 0;
		t_VRP[0][3] = -x_VRP;

		t_VRP[1][0] = 0;
		t_VRP[1][1] = 1;
		t_VRP[1][2] = 0;
		t_VRP[1][3] = -y_VRP;

		t_VRP[2][0] = 0;
		t_VRP[2][1] = 0;
		t_VRP[2][2] = 1;
		t_VRP[2][3] = -z_VRP;

		t_VRP[3][0] = 0;
		t_VRP[3][1] = 0;
		t_VRP[3][2] = 0;
		t_VRP[3][3] = 1;

		//Rotation
		float [][] rotation = new float[4][4];

		float length_VPN = length(x_VPN, y_VPN, z_VPN);

		float r1_z = x_VPN/length_VPN;
		float r2_z = y_VPN/length_VPN;
		float r3_z = z_VPN/length_VPN;

		List<Float> result_Rx = cross_product(x_VUP, y_VUP, z_VUP, r1_z, r2_z, r3_z);

		float length_cross_Rx = length(result_Rx.get(0), result_Rx.get(1), result_Rx.get(2));

		float r1_x = result_Rx.get(0)/length_cross_Rx;
		float r2_x = result_Rx.get(1)/length_cross_Rx;
		float r3_x = result_Rx.get(2)/length_cross_Rx;

		List<Float> result_Ry = cross_product(r1_z, r2_z, r3_z, r1_x, r2_x, r3_x);

		float r1_y = result_Ry.get(0);
		float r2_y = result_Ry.get(1);
		float r3_y = result_Ry.get(2);

		rotation[0][0] = r1_x;
		rotation[0][1] = r2_x;
		rotation[0][2] = r3_x;
		rotation[0][3] = 0;

		rotation[1][0] = r1_y;
		rotation[1][1] = r2_y;
		rotation[1][2] = r3_y;
		rotation[1][3] = 0;

		rotation[2][0] = r1_z;
		rotation[2][1] = r2_z;
		rotation[2][2] = r3_z;
		rotation[2][3] = 0;

		rotation[3][0] = 0;
		rotation[3][1] = 0;
		rotation[3][2] = 0;
		rotation[3][3] = 1;

		//Shear
		float [][] shear = new float[4][4];

		shear[0][0] = 1;
		shear[0][1] = 0;
		shear[0][2] = ((0.5f * (umax + umin)) - x_PRP)/z_PRP;
		shear[0][3] = 0;

		shear[1][0] = 0;
		shear[1][1] = 1;
		shear[1][2] = ((0.5f * (vmax + vmin)) - y_PRP)/z_PRP;
		shear[1][3] = 0;

		shear[2][0] = 0;
		shear[2][1] = 0;
		shear[2][2] = 1;
		shear[2][3] = 0;

		shear[3][0] = 0;
		shear[3][1] = 0; 
		shear[3][2] = 0;
		shear[3][3] = 1;

		float [][] result_1 = multiply(rotation, t_VRP);

		//Translate and scale for parallel
		if(parallel)
		{
			float [][]t_par = new float[4][4];

			t_par[0][0] = 1;
			t_par[0][1] = 0;
			t_par[0][2] = 0;
			t_par[0][3] = -(umax + umin)/2;

			t_par[1][0] = 0;
			t_par[1][1] = 1;
			t_par[1][2] = 0;
			t_par[1][3] = -(vmax + vmin)/2;

			t_par[2][0] = 0;
			t_par[2][1] = 0;
			t_par[2][2] = 1;
			t_par[2][3] = -front_face;

			t_par[3][0] = 0;
			t_par[3][1] = 0;
			t_par[3][2] = 0;
			t_par[3][3] = 1;

			float [][] s_par = new float[4][4];

			s_par[0][0] = 2/(umax - umin);
			s_par[0][1] = 0;
			s_par[0][2] = 0;
			s_par[0][3] = 0;

			s_par[1][0] = 0;
			s_par[1][1] = 2/(vmax - vmin);
			s_par[1][2] = 0;
			s_par[1][3] = 0;

			s_par[2][0] = 0;
			s_par[2][1] = 0;
			s_par[2][2] = 1/(front_face - back_face);
			s_par[2][3] = 0;

			s_par[3][0] = 0;
			s_par[3][1] = 0;
			s_par[3][2] = 0;
			s_par[3][3] = 1;

			//Multiplications for parallel
			float [][] result_2 = multiply(shear, result_1);

			float [][] result_3 = multiply(t_par, result_2);

			float [][] N_par = multiply(s_par, result_3);

			for(int i=0; i<vertices.size(); i++)
			{
				float [][] temp = new float[4][1];

				temp[0][0] = vertices.get(i).get(0);
				temp[1][0] = vertices.get(i).get(1);
				temp[2][0] = vertices.get(i).get(2);
				temp[3][0] = vertices.get(i).get(3);

				float [][] result = multiply(N_par, temp);

				List<Float> row = new ArrayList<Float>();

				row.add(result[0][0]);
				row.add(result[1][0]);
				row.add(result[2][0]);

				vertices.set(i, row);
			}
		}

		else
		{
			//Translate to -prp
			float [][] t_prp = new float[4][4];

			t_prp[0][0] = 1;
			t_prp[0][1] = 0;
			t_prp[0][2] = 0;
			t_prp[0][3] = -x_PRP;

			t_prp[1][0] = 0;
			t_prp[1][1] = 1;
			t_prp[1][2] = 0;
			t_prp[1][3] = -y_PRP;

			t_prp[2][0] = 0;
			t_prp[2][1] = 0;
			t_prp[2][2] = 1;
			t_prp[2][3] = -z_PRP;

			t_prp[3][0] = 0;
			t_prp[3][1] = 0;
			t_prp[3][2] = 0;
			t_prp[3][3] = 1;

			//Scaling for perspective
			float [][] s_per = new float [4][4];

			s_per[0][0] = (2 * z_PRP)/((umax - umin) * (z_PRP - back_face));
			s_per[0][1] = 0;
			s_per[0][2] = 0;
			s_per[0][3] = 0;

			s_per[1][0] = 0;
			s_per[1][1] = (2 * z_PRP)/((vmax - vmin) * (z_PRP - back_face));
			s_per[1][2] = 0;
			s_per[1][3] = 0;

			s_per[2][0] = 0;
			s_per[2][1] = 0;
			s_per[2][2] = 1/(z_PRP - back_face);
			s_per[2][3] = 0;

			s_per[3][0] = 0;
			s_per[3][1] = 0;
			s_per[3][2] = 0;
			s_per[3][3] = 1;

			//Multiplications for perspective

			float [][] result_2 = multiply(t_prp, result_1);
			float [][] result_3 = multiply(shear, result_2);
			float [][] N_per = multiply(s_per, result_3);		

			for(int i=0; i<vertices.size(); i++)
			{
				float [][] temp = new float[4][1];

				temp[0][0] = vertices.get(i).get(0);
				temp[1][0] = vertices.get(i).get(1);
				temp[2][0] = vertices.get(i).get(2);
				temp[3][0] = vertices.get(i).get(3);

				float [][] result = multiply(N_per, temp);

				List<Float> row = new ArrayList<Float>();

				row.add(result[0][0]);
				row.add(result[1][0]);
				row.add(result[2][0]);

				vertices.set(i, row);
			}

		}

	}

	public void read_file(String input, int color) throws FileNotFoundException
	{
		File file = new File(input);		
		Scanner sc = new Scanner(file);

		while(sc.hasNextLine())
		{
			String line = sc.nextLine();
			String parse[] = line.split(" ");

			if(line.trim().length() != 0 && parse[0].equals("v"))
			{
				List<Float> row = new ArrayList<Float>();
				float x = Float.parseFloat(parse[1]);
				float y = Float.parseFloat(parse[2]);
				float z = Float.parseFloat(parse[3]);

				row.add(x);
				row.add(y);
				row.add(z);
				row.add(1.0f);

				vertices.add(row);
			}

			if(line.trim().length() != 0 && parse[0].equals("f"))
			{
				List<Integer> row = new ArrayList<Integer>();

				int x = Integer.parseInt(parse[1]);
				int y = Integer.parseInt(parse[2]);
				int z = Integer.parseInt(parse[3]);

				row.add(x-1);
				row.add(y-1);
				row.add(z-1);
				row.add(color);

				faces.add(row);
			}

		}

		sc.close();   
	}

	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException
	{
		CG_hw5 obj = new CG_hw5();

		for (int i=0; i<args.length; i+=2)
		{
			if(args[i].equals("-f"))
				obj.input1 = args[i+1];

			if(args[i].equals("-g"))
				obj.input2 = args[i+1];

			if(args[i].equals("-i"))
				obj.input3 = args[i+1];

			if(args[i].equals("-x"))
				obj.x_PRP = Float.parseFloat(args[i+1]);

			if(args[i].equals("-y"))
				obj.y_PRP = Float.parseFloat(args[i+1]);

			if(args[i].equals("-z"))
				obj.z_PRP = Float.parseFloat(args[i+1]);

			if(args[i].equals("-X"))
				obj.x_VRP = Float.parseFloat(args[i+1]);

			if(args[i].equals("-Y"))
				obj.y_VRP = Float.parseFloat(args[i+1]);

			if(args[i].equals("-Z"))
				obj.z_VRP = Float.parseFloat(args[i+1]);

			if(args[i].equals("-q"))
				obj.x_VPN = Float.parseFloat(args[i+1]);

			if(args[i].equals("-r"))
				obj.y_VPN = Float.parseFloat(args[i+1]);

			if(args[i].equals("-w"))
				obj.z_VPN = Float.parseFloat(args[i+1]);

			if(args[i].equals("-Q"))
				obj.x_VUP = Float.parseFloat(args[i+1]);

			if(args[i].equals("-R"))
				obj.y_VUP = Float.parseFloat(args[i+1]);

			if(args[i].equals("-W"))
				obj.z_VUP = Float.parseFloat(args[i+1]);

			if(args[i].equals("-u"))
				obj.umin = Float.parseFloat(args[i+1]);

			if(args[i].equals("-v"))
				obj.vmin = Float.parseFloat(args[i+1]);

			if(args[i].equals("-U"))
				obj.umax = Float.parseFloat(args[i+1]);

			if(args[i].equals("-V"))
				obj.vmax = Float.parseFloat(args[i+1]);

			if(args[i].equals("-j"))
				obj.view_x1 = Integer.parseInt(args[i+1]);

			if(args[i].equals("-k"))
				obj.view_y1 = Integer.parseInt(args[i+1]);

			if(args[i].equals("-o"))
				obj.view_x2 = Integer.parseInt(args[i+1]);

			if(args[i].equals("-p"))
				obj.view_y2 = Integer.parseInt(args[i+1]);

			if(args[i].equals("-P"))
				obj.parallel = true;

			if(args[i].equals("-F"))
				obj.front_face = Float.parseFloat(args[i+1]);

			if(args[i].equals("-B"))
				obj.back_face = Float.parseFloat(args[i+1]);
		}

		obj.d = obj.z_PRP/(obj.back_face - obj.z_PRP);

		obj.read_file(obj.input1,0);

		obj.width = 501;
		obj.height = 501;
		obj.pixels = new String[obj.height][obj.width];
		obj.zBuffer = new float[obj.height][obj.width];

		//Initialing pixel and zbuffer
		for (int i=0; i<obj.height; i++)
		{
			for (int j=0; j<obj.width; j++)
			{
				obj.pixels[i][j] = "0";
				obj.zBuffer[i][j] = -1;
			}	
		}
		obj.transformations();
		obj.projection();
		obj.faces_to_polygons();
		obj.viewport_transformation();
		obj.polygon_filling();

		if(!obj.input2.isEmpty())
		{
			obj.vertices = new ArrayList<List<Float>>();
			obj.faces = new ArrayList<List<Integer>>();
			obj.all_polygons = new ArrayList<List<List<Float>>>();
			obj.viewport_polygons = new ArrayList<List<List<Float>>>();
			obj.intersection_points = new ArrayList<List<List<Float>>>();
			obj.edge_list = new ArrayList<List<List<Float>>>();
			obj.red = new ArrayList<String>();
			obj.green = new ArrayList<String>();
			obj.blue = new ArrayList<String>();
			obj.read_file(obj.input2,1);


			obj.transformations();
			obj.projection();
			obj.faces_to_polygons();
			obj.viewport_transformation();
			obj.polygon_filling();

		}

		if(!obj.input3.isEmpty())
		{
			obj.vertices = new ArrayList<List<Float>>();
			obj.faces = new ArrayList<List<Integer>>();
			obj.all_polygons = new ArrayList<List<List<Float>>>();
			obj.viewport_polygons = new ArrayList<List<List<Float>>>();
			obj.intersection_points = new ArrayList<List<List<Float>>>();
			obj.edge_list = new ArrayList<List<List<Float>>>();
			obj.red = new ArrayList<String>();
			obj.green = new ArrayList<String>();
			obj.blue = new ArrayList<String>();
			obj.read_file(obj.input3,2);



			obj.transformations();
			obj.projection();
			obj.faces_to_polygons();
			obj.viewport_transformation();
			obj.polygon_filling();
		}

		obj.output();
	}
}
