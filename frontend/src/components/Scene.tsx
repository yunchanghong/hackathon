import "../App.css";
import * as THREE from "three";
import { Canvas } from "@react-three/fiber";
import { useLoader } from "@react-three/fiber";
import { Environment, OrbitControls, useGLTF } from "@react-three/drei";
import { OBJLoader } from "three/examples/jsm/loaders/OBJLoader";
import { MTLLoader } from "three/examples/jsm/loaders/MTLLoader";
import { DDSLoader } from "three-stdlib";
import { Suspense } from "react";

THREE.DefaultLoadingManager.addHandler(/\.dds$/i, new DDSLoader());


const Model = () => {
  const materials = useLoader(MTLLoader, "/public/mesh.mtl");
  const obj = useLoader(OBJLoader, "/public/mesh.obj", (loader) => {
    materials.preload();
    loader.setMaterials(materials);
  });

  console.log(obj);
  return <primitive object={obj} scale={19.8} />;
};

function Scene() {
  return (
    <div className="App">
      <Canvas>
        <Suspense fallback={null}>
          {/* <Model /> */}
          <primitive  object={useGLTF('/public/test.glb').scene}/>
          <OrbitControls />
          <Environment preset="sunset" background/>
        </Suspense>
      </Canvas>
    </div>
  );
}

export default Scene;  


// import React, { Suspense } from "react";
// import { Canvas } from "@react-three/fiber";
// import { OrbitControls, useGLTF } from "@react-three/drei";
// import * as THREE from "three";

// // This component handles the loading and displaying of your GLB model
// function WorldModel({ path }) {
// 	const { nodes, materials, scene } = useGLTF(path);
// 	return <primitive object={scene} />;

// 	// tried nodes.Scene but the scene still looks greyscale
// 	// return <primitive object={nodes.Scene} />;

// 	// tried passing materials to the primitive but the scene still looks greyscale
// 	// return <primitive object={scene} material={materials} />;
// }

// function Scene() {
// 	return (
// 		<div style={{ height: "100vh" }}>
// 			{/* <Canvas gl={{ antialias: true, toneMapping: THREE.NoToneMapping }} linear>
// 				<ambientLight intensity={1} />
// 				<directionalLight position={[0, 10, 5]} intensity={1} />

// 				<WorldModel path="../assets/test.glb" />

// 				<OrbitControls />
// 			</Canvas> */}
      
//         <Canvas>
//           <Suspense fallback={null}>
//           <WorldModel path="../assets/test.glb" />
//             <OrbitControls />
//             {/* <Environment preset="studio" background /> */}
//           </Suspense>
//         </Canvas>

// 		</div>
// 	);
// }

// export default Scene;

// useGLTF.preload("../assets/test.glb"); 

