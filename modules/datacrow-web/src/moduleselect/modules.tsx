import React, { useState, useEffect } from 'react';

const Modules = () => {
  const [data, setData] = useState([]);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const response = await fetch('http://localhost:8080/datacrow/api/modules/');
        const result = await response.json();
        setData(result);
      } catch (error) {
        console.error('Error fetching data:', error);
      }
    };

    fetchData();
  }, []);

  return (
    <div>
      <ul>
        {data.map((item) => (
          <li key={item.index}>{item.name}</li>
        ))}
      </ul>
    </div>
  );
};

export default Modules;