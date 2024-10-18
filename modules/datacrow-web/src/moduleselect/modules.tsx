import React, { useState, useEffect } from 'react';
import { fetchModules, type Module } from '../api/datacrow_api';

function ModuleList() {
  const [modules, setModules] = useState<Module[]>([]);
  useEffect(() => {
    fetchModules().then((data) => setModules(data));
  }, []);

  return (
    <div>
      <ul>
        {modules.map((item) => (
          <li key={item.index}>{item.name}</li>
        ))}
      </ul>
    </div>
  );
};

export default ModuleList;